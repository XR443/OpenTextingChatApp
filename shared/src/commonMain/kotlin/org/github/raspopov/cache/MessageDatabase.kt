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
            .executeAsOne().toDomain()
            .apply { payload = CryptoPlatform.decrypt(masterKeys!!.key, payload!!) }
    }


    internal fun getAllMessagesForUsers(author: Uuid, recipient: Uuid): List<Message> {
        val authorStr = author.toString()
        val recipientStr = recipient.toString()
        return messagesQuery.getDialogMessages(
            accountId, authorStr, recipientStr, recipientStr, authorStr
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
                CryptoPlatform.encrypt(masterKeys!!.key, message.payload!!),
                message.type.toString(),
                message.status.toString(),
                toTimestamp(message.created),
                toTimestamp(message.updated),
            )
        }
    }

    private fun List<Messages>.toDomainList() = this.asSequence()
        .toDomain()
        // todo работает только для текста
        .map { it.apply { payload = CryptoPlatform.decrypt(masterKeys!!.key, payload!!) } }
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

}