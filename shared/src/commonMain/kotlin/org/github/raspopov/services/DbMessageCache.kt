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

    override fun getMessageByInternalId(id: Uuid): Message {
        return messageDatabase.getMessageByInternalId(id)
    }

    override fun getMessagesAuthoredBy(id: Uuid): List<Message> {
        return messageDatabase.getAllMessagesAuthoredBy(id)
    }

    override fun getMessageBetween(
        user1: Uuid,
        user2: Uuid,
        internalMessageId: Uuid?,
        limit: Long
    ): List<Message> {
        val accountId = settingsViewModel.mainAccount.value!!.id
        return if (internalMessageId != null) {
            // Case 1: internalMessageId is provided - get messages before and after it
            val targetMessage = messageDatabase.getMessageByInternalId(internalMessageId)

            // Get messages newer than target (before it in sorted order)
            val beforeMessages = messageDatabase.getMessagesBetweenUsersBefore(
                user1 = user1,
                user2 = user2,
                accountId = accountId,
                beforeMessageId = internalMessageId,
                limit = limit
            )

            // Get messages older than target (after it in sorted order)
            val afterMessages = messageDatabase.getMessagesBetweenUsersAfter(
                user1 = user1,
                user2 = user2,
                accountId = accountId,
                afterMessageId = internalMessageId,
                limit = limit
            )

            // Combine and sort: newer messages + target + older messages
            (beforeMessages + listOf(targetMessage) + afterMessages)
                .sortedByDescending { it.created }
        } else {
            // Case 2: No internalMessageId - try to find first unread message
            val firstUnread = getFirstUnreadMessageForUsers(user1, user2)

            if (firstUnread != null) {
                // Found unread message - get messages around it
                val beforeMessages = messageDatabase.getMessagesBetweenUsersBefore(
                    user1 = user1,
                    user2 = user2,
                    accountId = accountId,
                    beforeMessageId = firstUnread.internalId,
                    limit = limit
                )

                val afterMessages = messageDatabase.getMessagesBetweenUsersAfter(
                    user1 = user1,
                    user2 = user2,
                    accountId = accountId,
                    afterMessageId = firstUnread.internalId,
                    limit = limit
                )

                (beforeMessages + listOf(firstUnread) + afterMessages)
                    .sortedByDescending { it.created }
            } else {
                // Case 3: No unread messages - return latest messages (limit * 2)
                messageDatabase.getLatestMessagesBetweenUsers(
                    user1 = user1,
                    user2 = user2,
                    accountId = accountId,
                    limit = limit
                ).sortedByDescending { it.created }
            }
        }
    }

    override fun getUnsent(
        user1: Uuid
    ): List<Message> {
        return messageDatabase.getUnsent(user1)
    }

    override fun update(messages: List<Message>) {
        messages.forEach { update(it) }
    }

    override fun update(message: Message) {
        messageDatabase.updateMessage(message)
    }

    override fun markRead(messageIds: List<Uuid>): List<Message> {
        val mainId = settingsViewModel.mainAccount.value!!.id
        return messageDatabase.markRead(messageIds, mainId)
    }

    override fun getFirstUnreadMessageForUsers(
        user1Id: Uuid,
        user2Id: Uuid
    ): Message? {
        val accountId = settingsViewModel.mainAccount.value!!.id
        return messageDatabase.getFirstUnreadMessageBetweenUsers(
            user1 = user1Id,
            user2 = user2Id,
            accountId = accountId
        )
    }

}