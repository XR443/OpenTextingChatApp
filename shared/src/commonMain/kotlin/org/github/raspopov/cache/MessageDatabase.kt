package org.github.raspopov.cache

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.github.raspopov.domain.MasterKeys
import org.github.raspopov.domain.Message
import org.github.raspopov.entity.Messages
import org.github.raspopov.extensions.toDomain
import org.github.raspopov.services.platform.CryptoPlatform
import org.github.raspopov.views.MasterPasswordViewModel
import org.github.raspopov.views.SettingsViewModel
import kotlin.time.Clock
import kotlin.uuid.Uuid


internal class MessageDatabase(
    databaseDriverFactory: DatabaseDriverFactory,
    private val settingsViewModel: SettingsViewModel,
    private val masterPasswordViewModel: MasterPasswordViewModel,
) {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val messagesCrud = database.messagesCRUDQueries
    private val messagesUpdates = database.messagesUpdatesQueries
    private val messagesQuery = database.messagesQueriesQueries

    private val messagesFilters = database.messagesFiltersQueries

    private val masterKeys: MasterKeys?
        get() = masterPasswordViewModel.uiState.value.keys

    private val accountId: String
        get() = settingsViewModel.mainAccount.value!!.id.toString()

    internal fun getMessageByInternalId(internalId: Uuid): Message {
        val internalIdStr = internalId.toString()
        return messagesCrud.getMessageByInternalId(internalIdStr)
            .executeAsOne()
            .toDomain()
            .apply { decryptPayload() }
    }


    internal fun getAllMessagesForUsers(
        author: Uuid,
        recipient: Uuid,
        offset: Long,
        limit: Long
    ): List<Message> {
        val authorStr = author.toString()
        val recipientStr = recipient.toString()
        return messagesQuery.getDialogMessages(
            accountId, authorStr, recipientStr, recipientStr, authorStr,
            limit, offset
        )
            .executeAsList()
            .toDomainList()
    }


    internal fun getAllMessagesAuthoredBy(author: Uuid): List<Message> {
        val authorStr = author.toString()
        return messagesQuery.getMessagesAuthoredByUser(accountId, authorStr)
            .executeAsList()
            .toDomainList()

    }

    internal fun updateMessage(message: Message) {
        val messageFromDb = messagesCrud.getMessageById(accountId, message.id.toString())
            .executeAsOneOrNull()

        if (messageFromDb != null) {
            val payload = message.payload ?: messageFromDb.payload
            messagesUpdates.updateMessagePayloadWithStatus(
                CryptoPlatform.encrypt(masterKeys!!.key, payload!!),
                message.status.toString(),
                toTimestamp(message.updated),
                accountId, message.id.toString()
            )
        } else {
            // todo
            messagesCrud.insertMessage(
                message.id.toString(),
                accountId,
                message.internalId.toString(),
                message.author.toString(),
                message.recipient.toString(),
                // todo прятать под ключ сообщения
                if (message.contentType.encrypted) CryptoPlatform.encrypt(
                    masterKeys!!.key,
                    message.payload!!
                ) else message.payload,
                message.contentType.fullType,
                message.status.toString(),
                toTimestamp(message.created),
                toTimestamp(message.updated),
                message.payloadKey
            )
        }
    }

    private fun List<Messages>.toDomainList() = this.asSequence()
        .toDomain()
        .map { it.apply { decryptPayload() } }
        .toList()

    private fun toTimestamp(localDateTime: LocalDateTime?): Long {
        val instant = localDateTime?.toInstant(TimeZone.currentSystemDefault())
        val timestampUpdated = instant?.toEpochMilliseconds()
        return timestampUpdated ?: Clock.System.now().toEpochMilliseconds()
    }

    fun markRead(messageIds: List<Uuid>, mainId: Uuid): List<Message> {
        val author = mainId.toString()
        val ids = messageIds.map { it.toString() }
        messagesUpdates.markMessageAsRead(ids, author)
        return messagesFilters.getMessagesByIdAndAuthor(ids, author)
            .executeAsList()
            .toDomainList()
    }

    fun getMessagesBetweenUsersBefore(
        user1: Uuid,
        user2: Uuid,
        beforeMessageId: Uuid,
        limit: Long,
        accountId: Uuid
    ): List<Message> {
        val user1Str = user1.toString()
        val user2Str = user2.toString()
        val beforeMessageIdStr = beforeMessageId.toString()
        val accountIdStr = accountId.toString()

        return messagesFilters.getMessagesBetweenUsersBefore(
            accountIdStr,
            user1Str,
            user2Str,
            user2Str,
            user1Str,
            beforeMessageIdStr,
            limit
        ).executeAsList()
            .toDomainList()
    }

    fun getMessagesBetweenUsersAfter(
        user1: Uuid,
        user2: Uuid,
        afterMessageId: Uuid,
        limit: Long,
        accountId: Uuid
    ): List<Message> {
        val user1Str = user1.toString()
        val user2Str = user2.toString()
        val afterMessageIdStr = afterMessageId.toString()
        val accountIdStr = accountId.toString()

        return messagesFilters.getMessagesBetweenUsersAfter(
            accountIdStr,
            user1Str,
            user2Str,
            user2Str,
            user1Str,
            afterMessageIdStr,
            limit
        ).executeAsList()
            .toDomainList()
    }

    fun getLatestMessagesBetweenUsers(
        user1: Uuid,
        user2: Uuid,
        limit: Long,
        accountId: Uuid
    ): List<Message> {
        val user1Str = user1.toString()
        val user2Str = user2.toString()
        val accountIdStr = accountId.toString()

        return messagesFilters.getLatestMessagesBetweenUsers(
            accountIdStr,
            user1Str,
            user2Str,
            user2Str,
            user1Str,
            limit
        ).executeAsList()
            .toDomainList()
    }

    fun getFirstUnreadMessageBetweenUsers(
        user1: Uuid,
        user2: Uuid,
        accountId: Uuid
    ): Message? {
        val user1Str = user1.toString()
        val user2Str = user2.toString()
        val accountIdStr = accountId.toString()

        return messagesFilters.getFirstUnreadMessageBetweenUsers(
            accountIdStr,
            user1Str,
            user2Str,
            user2Str,
            user1Str,
            accountIdStr
        ).executeAsOneOrNull()
            ?.toDomain()
            ?.apply { decryptPayload() }
    }

    private fun Message.decryptPayload() {
        if (payloadKey != null) {
            // todo need a password of chat
            val payloadKeyDecrypted = CryptoPlatform.decrypt(masterKeys!!.key, payloadKey!!)
            payload = CryptoPlatform.decrypt(payloadKeyDecrypted, payload!!)
        } else {
            payload = CryptoPlatform.decrypt(masterKeys!!.key, payload!!)
        }
    }

}