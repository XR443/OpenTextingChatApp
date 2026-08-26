package org.github.raspopov.domain

data class MasterKeys(
    val key: ByteArray,    // Для аутентификации (SHA-256)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MasterKeys

        if (!key.contentEquals(other.key)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.contentHashCode()
        return result
    }
}