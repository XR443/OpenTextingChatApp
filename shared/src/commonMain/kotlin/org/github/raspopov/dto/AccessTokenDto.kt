package org.github.raspopov.dto

import kotlinx.serialization.Serializable

@Serializable
data class AccessTokenDto(
    val token: String,
    val type: String
)
