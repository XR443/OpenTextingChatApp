package org.github.raspopov.services

import kotlinx.coroutines.flow.Flow
import org.github.raspopov.domain.Message
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
interface MessageService {
    suspend fun sendUnsent()
    suspend fun send(message: Message): Message
    suspend fun create(text: String, to: Uuid): Flow<Message?>

    suspend fun findLastMessageWithUser(userId: Uuid): Flow<Message?>

    suspend fun markMessageAsRead(
        internalId: Uuid
    ): Flow<List<Message>>

    // Получение сообщений вокруг определенной даты
    suspend fun getMessagesAround(
        userId: Uuid,
        internalMessage: Message?,
        limit: Long
    ): Flow<List<Message>>

    suspend fun getMessagesAroundFirstUnread(
        userId: Uuid,
        limit: Long
    ): Flow<List<Message>>
}
