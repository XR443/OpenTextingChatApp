package org.github.raspopov.dto

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

// todo сделать настраиваемым
@Serializable
data class MessagesDataDto(
    val ids: List<Uuid>
)
