package org.github.raspopov.services

import org.github.raspopov.domain.Message
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
interface MessageCache {
    fun getMessageByInternalId(id: Uuid): Message
    fun getMessagesAuthoredBy(id: Uuid): List<Message>
    fun getMessageBetween(
        user1: Uuid, user2: Uuid,
        internalMessageId: Uuid? = null, limit: Long = 50
    ): List<Message>
    fun getUnsent(
        user1: Uuid
    ): List<Message>

    fun update(messages: List<Message>)
    fun update(message: Message)
    fun markRead(messageIds: List<Uuid>): List<Message>
    fun getFirstUnreadMessageForUsers(user1Id: Uuid, user2Id: Uuid): Message?

}
