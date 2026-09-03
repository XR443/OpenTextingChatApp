package org.github.raspopov.dto

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// todo сделать JsonAlias
@Serializable
@ExperimentalUuidApi
data class MessageDto(
    val id: Uuid, // UUID как String для сериализации
    val authorId: Uuid,
    val recipientId: Uuid,
    val format: String, // Type в строковом представлении
    val status: String, // Status в строковом представлении
    val createdAt: String, // ISO datetime
    val updatedAt: String? = null
)

@Serializable
data class CreateMessageRequest(
    val text: String,
    val recipientId: String
)

@Serializable
data class MessagesResponse(
    val messages: List<MessageDto>
)