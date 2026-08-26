package org.github.raspopov.dto

import kotlin.uuid.Uuid

data class EncryptedMessage(
    val id: Uuid,
    val size: Int,
    val content: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as EncryptedMessage

        if (size != other.size) return false
        if (id != other.id) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size
        result = 31 * result + id.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}
