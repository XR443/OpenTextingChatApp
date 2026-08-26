package org.github.raspopov.services

import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.sync.Mutex
import org.github.raspopov.domain.Message
import kotlin.sequences.forEach
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
interface MessageCache {
    suspend fun getMessageByInternalId(id: Uuid): Message
    suspend fun getMessagesAuthoredBy(id: Uuid): List<Message>
    suspend fun getMessageBetween(user1: Uuid, user2: Uuid): List<Message>
    suspend fun update(messages: List<Message>)
    suspend fun update(message: Message)
    suspend fun markRead(messageIds: kotlin.collections.List<kotlin.uuid.Uuid>): List<Message>
}
