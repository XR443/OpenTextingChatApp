package org.github.raspopov.services

import org.github.raspopov.domain.Account
import org.github.raspopov.domain.MasterKeys
import org.github.raspopov.services.platform.CryptoPlatform

class KeyDerivationService {

    companion object {
        const val ITERATIONS = 310_000 // Рекомендация OWASP (2024)
        const val KEY_LENGTH = 32      // 256 бит
        const val SALT_LENGTH = 32     // 256 бит соли
    }

    private val salt: ByteArray by lazy {
        // В реальном приложении соль должна храниться отдельно
        generateSalt()
    }

    fun generateSalt(): ByteArray {
        return ByteArray(SALT_LENGTH).apply {
            CryptoPlatform.randomBytes(this)
        }
    }

    fun generateMasterKeys(masterPassword: String): MasterKeys {
        require(masterPassword.isNotBlank()) { "Master password cannot be empty" }
        // todo
//        require(masterPassword.length >= 8) { "Master password must be at least 8 characters" }

        val passwordBytes = masterPassword.encodeToByteArray()

        val authSalt = salt

        return MasterKeys(
            key = CryptoPlatform.pbkdf2(passwordBytes, authSalt, ITERATIONS, KEY_LENGTH)
        )
    }

    fun generateMasterKeys(masterPassword: String, account: Account): MasterKeys {
        require(masterPassword.isNotBlank()) { "Master password cannot be empty" }
        // todo
//        require(masterPassword.length >= 8) { "Master password must be at least 8 characters" }

        val passwordBytes = masterPassword.encodeToByteArray()

        return MasterKeys(
            key = CryptoPlatform.pbkdf2(passwordBytes, account.salt!!, ITERATIONS, KEY_LENGTH),
        )
    }

    fun verifyMasterPassword(
        masterPassword: String,
        expectedAuthKey: ByteArray,
    ): Boolean {
        val keys = generateMasterKeys(masterPassword)
        return keys.key.contentEquals(expectedAuthKey)
    }
}