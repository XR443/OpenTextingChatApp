package org.github.raspopov.services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.header
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
import org.github.raspopov.domain.Message
import org.github.raspopov.domain.Status
import org.github.raspopov.domain.Type
import org.github.raspopov.dto.EncryptedMessage
import org.github.raspopov.dto.LimitDto
import org.github.raspopov.dto.MessageDto
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
//    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : MessageService {

    private val mainAccount
        get() = settings.mainAccount.value!!
    private val baseUrl
        get() = mainAccount.host
    private val masterKeys
        get() = masterPasswordViewModel.uiState.value.keys


    // todo переделать чтоб не ходить каждый раз за токеном
    private suspend fun token(): String {
        return accountService.login(mainAccount)!!.token
    }

    override suspend fun create(
        text: String,
        to: Uuid
    ): Flow<Message> = flow {
        // todo different message types
        val url = "$baseUrl/messages/text/to/$to"

        val tokenVal = token()
        val trimmedText = text.trim()
        val bytesOfText = trimmedText.toByteArray(Charsets.UTF_8)
        val masterKey = masterKeys!!.key
        val bytesToSend = E2eeCrypto.encrypt(
            CryptoPlatform.decrypt(masterKey, mainAccount.privateKey!!),
            mainAccount.publicKey!!,
            keysService.getPublicKeyOf(to),
            bytesOfText
        )

        val response = client.post(url) {
            header(HttpHeaders.Authorization, "Bearer $tokenVal")
            contentType(Application.OctetStream)
//            setBody(json.encodeToString(request))
            // todo кодировки
            setBody(bytesToSend)
        }

        val messageDto = response.body<MessageDto>()
        emit(mapToDomain(messageDto, bytesOfText))
    }.flowOn(Dispatchers.Main)


    override suspend fun findMessagesWithUser(
        id: Uuid
    ): Flow<List<Message>> = flow {
        emit(getMessagesOfUserAndMy(id))
        emit(findMessagesWithUser(id, LimitDto(0, 50)))
    }.flowOn(Dispatchers.Main)

    private suspend fun getMessagesOfUserAndMy(id: Uuid): List<Message> {
        return messageCache.getMessageBetween(mainAccount.id, id)
    }

    private suspend fun findMessagesWithUser(
        userId: Uuid,
        limit: LimitDto,
    ): List<Message> {
        val infoUrl = "$baseUrl/messages/with/$userId"

        val tokenVal = token()

        val response = client.post(infoUrl) {
            header(HttpHeaders.Authorization, "Bearer $tokenVal")
            contentType(Application.Json)
            if (limit.start != null)
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

                val masterKey = masterKeys?.key ?: return listOf()
                val domainMessages = messages.map { m ->
                    mapToDomain(
                        m,
                        parsed[m.id]!!.content.run {
                            E2eeCrypto.decrypt(
                                CryptoPlatform.decrypt(masterKey, mainAccount.privateKey!!),
                                mainAccount.publicKey!!,
                                keysService.getPublicKeyOf(userId),
                                this
                            )
                        }
                    )
                }
                messageCache.update(domainMessages)
            }
        }
        return getMessagesOfUserAndMy(userId)
    }

    /**
     * Парсинг байтового потока
     */
    private fun parseMessages(data: ByteArray): Map<Uuid, EncryptedMessage> {
        val messages = mutableMapOf<Uuid, EncryptedMessage>()
        var offset = 0

        while (offset + 20 <= data.size) {
            val startOffset = offset

            try {
                // 1. Читаем UUID (16 байт)
                val uuidBytes = data.copyOfRange(offset, offset + 16)
                offset += 16
                val uuid = Uuid.fromByteArray(uuidBytes)

                // 2. Читаем размер (4 байта, знаковый int, big-endian)
                val sizeBytes = data.copyOfRange(offset, offset + 4)
                offset += 4
                val msgSize = bytesToInt(sizeBytes)

                // Проверяем валидность размера
                if (msgSize < 0 || msgSize > 10 * 1024 * 1024) {
                    throw IllegalArgumentException("Invalid message size: $msgSize")
                }

                if (offset + msgSize > data.size) {
                    throw IllegalArgumentException("Incomplete message data")
                }

                // 3. Читаем сообщение
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

    /**
     * Конвертация 4 байт в Int (big-endian)
     */
    private fun bytesToInt(bytes: ByteArray): Int {
        return ((bytes[0].toInt() and 0xFF) shl 24) or
                ((bytes[1].toInt() and 0xFF) shl 16) or
                ((bytes[2].toInt() and 0xFF) shl 8) or
                (bytes[3].toInt() and 0xFF)
    }

    override suspend fun findLastMessageWithUser(
        id: Uuid
    ): Flow<Message?> = flow {
        emit(getMessagesOfUserAndMy(id).maxByOrNull { it.created })
        emit(findMessagesWithUser(id, LimitDto(0, 1)).maxByOrNull { it.created })
    }.flowOn(Dispatchers.Main)

    override suspend fun findMessagesWithUserFromNthMessagePlusDelta(
        id: Uuid,
        n: Int,
        delta: Int
    ): Flow<List<Message>> = flow {
        val myMessages = messageCache.getMessagesAuthoredBy(mainAccount.id).asSequence()
        val otherMessages = messageCache.getMessagesAuthoredBy(id).asSequence()
        emit(
            (myMessages + otherMessages)
                .sortedByDescending { it.created }
                .toList()
                .subList(n, n + delta)
        )
        emit(findMessagesWithUser(id, LimitDto(n, delta)))
    }.flowOn(Dispatchers.Main)

    override suspend fun markMessageAsRead(
        internalId: Uuid
    ): Flow<List<Message>> = flow {
        val message = messageCache.getMessageByInternalId(internalId)
        val url = "$baseUrl/messages/read/${message.id}"

        val tokenVal = token()

        val response = client.post(url) {
            header(HttpHeaders.Authorization, "Bearer $tokenVal")
            contentType(Application.Json)
        }

        val messageDto = response.body<List<MessageDto>>()
        val messageIds = messageDto.map { it.id }
        val messages = messageCache.markRead(messageIds)
        emit(messages)
    }.flowOn(Dispatchers.Main)

    // Приватный метод для маппинга DTO в доменную модель
    private fun mapToDomain(dto: MessageDto, payload: ByteArray): Message {
        return Message(
            id = dto.id,
            internalId = Uuid.random(),
            author = dto.authorId,
            recipient = dto.recipientId,
            payload = payload,
            type = mapType(dto.payloadType),
            status = mapStatus(dto.status),
            created = parseDateTime(dto.createdAt),
            updated = dto.updatedAt?.let { parseDateTime(it) } ?: Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
        )
    }

    private fun mapType(payloadType: String): Type {
        return when (payloadType.lowercase()) {
            "text" -> Type.TEXT
            "image" -> Type.IMAGE
            "audio" -> Type.AUDIO
            "video" -> Type.VIDEO
            else -> Type.TEXT // default
        }
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