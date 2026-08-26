package org.github.raspopov.services.platform

const val ALGORITHM = "EC"
const val CURVE = "secp256r1" // NIST P-256
const val KEY_SIZE = 256

/**
 * Класс-обертка для удобного хранения ключей
 */
data class KeyPairWrapper(val privateKey: ByteArray, val publicKey: ByteArray)

expect object E2eeCrypto {
    fun generateKeyPair(): KeyPairWrapper
    fun encrypt(
        myPrivateKey: ByteArray,
        myPublicKey: ByteArray,
        otherPublicKey: ByteArray,
        data: ByteArray
    ): ByteArray

    fun decrypt(
        myPrivateKey: ByteArray,
        myPublicKey: ByteArray,
        otherPublicKey: ByteArray,
        encryptedData: ByteArray
    ): ByteArray
}