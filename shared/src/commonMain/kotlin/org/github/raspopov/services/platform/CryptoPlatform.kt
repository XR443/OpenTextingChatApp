package org.github.raspopov.services.platform

const val IV_LENGTH = 12 // 12 байт для GCM
const val TAG_LENGTH = 128 // 128 бит тег аутентификации

const val AES_ALGORITHM = "AES/GCM/NoPadding"

expect object CryptoPlatform {
    fun pbkdf2(
        password: ByteArray,
        salt: ByteArray,
        iterations: Int,
        keyLength: Int
    ): ByteArray

    fun encrypt(key: ByteArray, data: ByteArray): ByteArray

    fun decrypt(key: ByteArray, encryptedData: ByteArray): ByteArray
    fun randomBytes(array: ByteArray)

}

