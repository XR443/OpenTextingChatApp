package org.github.raspopov.dto

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

// todo сделать настраиваемым
@Serializable
data class LimitDto(
    val start: Int?,
    val delta: Int?
)
