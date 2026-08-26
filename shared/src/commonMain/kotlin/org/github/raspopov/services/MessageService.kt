package org.github.raspopov.services

import kotlinx.coroutines.flow.Flow
import org.github.raspopov.domain.Message
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
interface MessageService {
    suspend fun create(text: String, to: Uuid): Flow<Message>

    suspend fun findMessagesWithUser(id: Uuid): Flow<List<Message>>

    suspend fun findLastMessageWithUser(id: Uuid): Flow<Message?>

    suspend fun findMessagesWithUserFromNthMessagePlusDelta(
        id: Uuid,
        n: Int,
        delta: Int
    ): Flow<List<Message>>

    suspend fun markMessageAsRead(
        internalId: Uuid
    ): Flow<List<Message>>

    // todo метод по поиску с датой
}
