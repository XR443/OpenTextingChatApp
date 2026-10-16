package org.github.raspopov.dto

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class PublicKeyInfo(val userId: Uuid, val username: String) {
}