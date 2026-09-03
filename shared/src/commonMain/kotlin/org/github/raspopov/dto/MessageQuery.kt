package org.github.raspopov.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

// todo сделать настраиваемым
@Serializable
data class MessageQuery(
    val messageId: Uuid? = null,
    val dateFrom: LocalDateTime? = null,
    val delta: Long = 50
)
