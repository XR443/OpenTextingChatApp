package org.github.raspopov.services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType.Application
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.github.raspopov.domain.ContentType
import org.github.raspopov.domain.Extra
import org.github.raspopov.domain.Message
import org.github.raspopov.domain.Status
import org.github.raspopov.dto.EncryptedMessage
import org.github.raspopov.dto.MessageDto
import org.github.raspopov.dto.MessageQuery
import org.github.raspopov.dto.MessagesDataDto
import org.github.raspopov.services.platform.CryptoPlatform
import org.github.raspopov.services.platform.E2eeCrypto
import org.github.raspopov.views.MasterPasswordViewModel
import org.github.raspopov.views.SettingsViewModel
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
class KtorMessageService(
    private val client: HttpClient,
    private val settings: SettingsViewModel,
    private val accountService: AccountService,
    private val messageCache: MessageCache,
    private val keysService: KeysService,
    private val masterPasswordViewModel: MasterPasswordViewModel,
) : MessageService {

    private val mainAccount
        get() = settings.mainAccount.value!!
    private val baseUrl
        get() = mainAccount.host
    private val masterKeys
        get() = masterPasswordViewModel.uiState.value.keys

    private suspend fun token(): String? {
        return accountService.login(mainAccount)?.token
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun sendUnsent() {
        var messages: List<Message> = messageCache.getUnsent(mainAccount.id)
        while (messages.isNotEmpty()) {
            for (messageToSend in messages) {
                send(messageToSend)
            }
            messages = messageCache.getUnsent(mainAccount.id)
        }
    }

    override suspend fun send(message: Message): Message {
        val url = "$baseUrl/messages/to/${message.recipient}"

        val contentType = message.contentType

        val tokenVal = token() ?: return message
        try {
            val masterKey = masterKeys!!.key
            val bytesToSend = E2eeCrypto.encrypt(
                CryptoPlatform.decrypt(masterKey, mainAccount.privateKey!!),
                mainAccount.publicKey!!,
                keysService.getPublicKeyOf(message.recipient),
                message.payload!!
            )

            val response = client.post(url) {
                header(HttpHeaders.Authorization, "Bearer $tokenVal")
                headers {
                    append("X-Content-Type", contentType.mimeType)
                    append("X-Encrypted", contentType.encrypted.toString())
                    if (contentType.compression != null) {
                        append("X-Compression", contentType.compression.name)
                    }
                    if (contentType.quality != null) {
                        append("X-Quality", contentType.quality.name)
                    }
                    if (!contentType.metadata.isNullOrEmpty()) {
                        this["X-Metadata"] = json.encodeToString(contentType.metadata)
                    }
                    if (contentType.extraParams.isNotEmpty()) {
                        this["X-Extra-Params"] = json.encodeToString(contentType.extraParams)
                    }
                }
                contentType(Application.OctetStream)
                setBody(bytesToSend)
            }

            val messageDto = response.body<MessageDto>()
            val mappedMessage = mapToDomain(
                messageDto,
                bytesToSend,
                messageDto.recipientId,
                message.internalId
            )
            if (mappedMessage != null) {
                messageCache.update(mappedMessage)
                return mappedMessage
            }
        } catch (e: Exception) {
            println("Exception was threw while trying to resend messages ${e.message}")
        }

        return message
    }

    override suspend fun create(
        text: String,
        to: Uuid,
        replyMessage: Message?
    ): Flow<Message?> = flow {
        val url = "$baseUrl/messages/to/$to"

        val trimmedText = text.trim()

        if (trimmedText.isEmpty()) {
            emit(null)
            return@flow
        }

        sendUnsent()

        val bytesOfText = trimmedText.toByteArray(Charsets.UTF_8)
        val contentType = ContentType.textEncrypted(extraParams = Extra.reply(replyMessage))

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val message = Message(
            id = null,
            internalId = Uuid.random(),
            author = mainAccount.id,
            recipient = to,
            payload = bytesOfText,
            contentType = contentType,
            status = Status.SENDING,
            created = now,
            updated = now,
            null // todo encryption key of payload. if null use master key
        ).apply { replied = replyMessage }
        messageCache.update(message)

        val sent = send(message)

        sent.replied = replyMessage
        emit(sent)
    }.flowOn(Dispatchers.Main)

    private suspend fun getMessagesOfUserAndMy(id: Uuid, query: MessageQuery): List<Message> {
        return messageCache.getMessageBetween(
            mainAccount.id, id,
            query.messageId, query.delta
        )
    }

    private suspend fun findMessagesWithUser(
        userId: Uuid,
        limit: MessageQuery,
    ): List<Message> {
        if (masterKeys?.key == null)
            return listOf()

        sendUnsent()

        //todo поиск по времени
        val infoUrl = "$baseUrl/messages/with/$userId"

        val tokenVal = token() ?: return listOf()

        val response = client.post(infoUrl) {
            header(HttpHeaders.Authorization, "Bearer $tokenVal")
            contentType(Application.Json)
            setBody(limit)
        }

        val messages = response.body<List<MessageDto>>()

        if (messages.isNotEmpty()) {
            val dataUrl = "$baseUrl/messages/data/stream"
            val dataStream = client.post(dataUrl) {
                header(HttpHeaders.Authorization, "Bearer $tokenVal")
                contentType(Application.Json)
                accept(Application.OctetStream)
                setBody(MessagesDataDto(messages.map { it.id }))
            }

            val bytes = dataStream.bodyAsBytes()
            if (bytes.isNotEmpty()) {
                val parsed = parseMessages(bytes)

                val domainMessages: List<Message> = messages.mapNotNull { m ->
                    mapToDomain(
                        m,
                        parsed[m.id]!!.content,
                        userId
                    )
                }
                messageCache.update(domainMessages)
            }
        }
        return messageCache.getMessageBetween(
            mainAccount.id, userId,
            limit.messageId,
            limit.delta
        )

    }

    private fun parseMessages(data: ByteArray): Map<Uuid, EncryptedMessage> {
        val messages = mutableMapOf<Uuid, EncryptedMessage>()
        var offset = 0

        while (offset + 20 <= data.size) {
            val startOffset = offset

            try {
                val uuidBytes = data.copyOfRange(offset, offset + 16)
                offset += 16
                val uuid = Uuid.fromByteArray(uuidBytes)

                val sizeBytes = data.copyOfRange(offset, offset + 4)
                offset += 4
                val msgSize = bytesToInt(sizeBytes)

                if (msgSize < 0 || msgSize > 10 * 1024 * 1024) {
                    throw IllegalArgumentException("Invalid message size: $msgSize")
                }

                if (offset + msgSize > data.size) {
                    throw IllegalArgumentException("Incomplete message data")
                }

                val content = data.copyOfRange(offset, offset + msgSize)
                offset += msgSize

                messages[uuid] = EncryptedMessage(id = uuid, size = msgSize, content = content)

            } catch (e: Exception) {
                println("Error parsing at offset $startOffset: ${e.message}")
                throw e
            }
        }

        return messages
    }

    private fun bytesToInt(bytes: ByteArray): Int {
        return ((bytes[0].toInt() and 0xFF) shl 24) or
                ((bytes[1].toInt() and 0xFF) shl 16) or
                ((bytes[2].toInt() and 0xFF) shl 8) or
                (bytes[3].toInt() and 0xFF)
    }

    override suspend fun findLastMessageWithUser(
        userId: Uuid
    ): Flow<Message?> = flow {
        getMessagesAround(
            userId, internalMessage = null, limit = 1
        ).collect { emit(it.firstOrNull()) }
    }.flowOn(Dispatchers.Main)

    override suspend fun markMessageAsRead(
        internalId: Uuid
    ): Flow<List<Message>> = flow {
        val message = messageCache.getMessageByInternalId(internalId)
        val url = "$baseUrl/messages/read/${message.id}"

        val tokenVal = token()

        if (tokenVal == null) {
            emit(listOf())
        } else {
            val response = client.post(url) {
                header(HttpHeaders.Authorization, "Bearer $tokenVal")
                contentType(Application.Json)
            }

            val messageDto = response.body<List<MessageDto>>()
            val messageIds = messageDto.map { it.id }
            val messages = messageCache.markRead(messageIds)
            emit(messages)
        }
    }.flowOn(Dispatchers.Main)

    override suspend fun getMessagesAround(
        userId: Uuid,
        internalMessage: Message?,
        limit: Long
    ): Flow<List<Message>> = flow {
//        val cachedMessages = messageCache.getMessageBetween(
//            mainAccount.id, userId,
//            internalMessage?.internalId,
//            limit
//        )
//        emit(cachedMessages)
        val messages = findMessagesWithUser(
            userId,
            MessageQuery(messageId = internalMessage?.id, delta = 50)
        )
        emit(messages)
    }.flowOn(Dispatchers.Main)

    override suspend fun getMessagesAroundFirstUnread(
        userId: Uuid,
        limit: Long
    ): Flow<List<Message>> = flow {
        getMessagesAround(
            userId, internalMessage = null, limit = limit
        ).collect { emit(it) }
    }.flowOn(Dispatchers.Main)

    // Приватный метод для маппинга DTO в доменную модель
    private fun mapToDomain(
        dto: MessageDto,
        payload: ByteArray,
        userId: Uuid,
        internalId: Uuid = Uuid.fromLongs(0, 0)
    ): Message? {
        val masterKey = masterKeys?.key ?: return null
        val contentType = ContentType.fromFullType(dto.format)
        val decryptedPayload = if (contentType.encrypted)
            E2eeCrypto.decrypt(
                CryptoPlatform.decrypt(masterKey, mainAccount.privateKey!!),
                mainAccount.publicKey!!,
                keysService.getPublicKeyOf(userId),
                payload
            ) else payload
        return Message(
            id = dto.id,
            internalId = internalId,
            author = dto.authorId,
            recipient = dto.recipientId,
            payload = decryptedPayload,
            contentType = contentType,
            status = mapStatus(dto.status),
            created = parseDateTime(dto.createdAt),
            updated = dto.updatedAt?.let { parseDateTime(it) } ?: Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()),
            null // todo encryption key of payload. if null use master key
        )
    }

    private fun mapStatus(status: String): Status {
        return when (status.lowercase()) {
            "sending" -> Status.SENDING
            "confirmed" -> Status.CONFIRMED
            "read" -> Status.READ
            else -> Status.UNKNOWN
        }
    }

    fun parseDateTime(utcString: String): LocalDateTime {
        val utcStringWithZ =
            if (!utcString.endsWith("Z") && (!utcString.contains("+") || !utcString.contains("-"))) {
                "${utcString}Z" // Используем -Z вместо +Z, т.к. это UTC (смещение 0)
            } else {
                utcString
            }
        return Instant.parse(utcStringWithZ).toLocalDateTime(TimeZone.currentSystemDefault())
    }
}