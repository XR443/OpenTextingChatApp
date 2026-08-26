package org.github.raspopov.services

import org.github.raspopov.cache.DatabaseDriverFactory
import org.github.raspopov.cache.MessageDatabase
import org.github.raspopov.domain.Message
import org.github.raspopov.views.MasterPasswordViewModel
import org.github.raspopov.views.SettingsViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@ExperimentalUuidApi
class DbMessageCacheImpl(
    databaseDriverFactory: DatabaseDriverFactory,
    masterPasswordViewModel: MasterPasswordViewModel,
    private val settingsViewModel: SettingsViewModel,
) : MessageCache {
    private val messageDatabase: MessageDatabase =
        MessageDatabase(databaseDriverFactory, settingsViewModel, masterPasswordViewModel)

    override suspend fun getMessageByInternalId(id: Uuid): Message {
        return messageDatabase.getMessageByInternalId(id)
    }

    override suspend fun getMessagesAuthoredBy(id: Uuid): List<Message> {
        return messageDatabase.getAllMessagesAuthoredBy(id)
    }

    override suspend fun getMessageBetween(
        user1: Uuid,
        user2: Uuid
    ): List<Message> {
        return messageDatabase.getAllMessagesForUsers(user1, user2)
    }

    override suspend fun update(messages: List<Message>) {
        messages.forEach { update(it) }
    }

    override suspend fun update(message: Message) {
        messageDatabase.updateMessage(message)
    }

    override suspend fun markRead(messageIds: List<Uuid>): List<Message> {
        val mainId = settingsViewModel.mainAccount.value!!.id
        return messageDatabase.markRead(messageIds, mainId)
    }

}