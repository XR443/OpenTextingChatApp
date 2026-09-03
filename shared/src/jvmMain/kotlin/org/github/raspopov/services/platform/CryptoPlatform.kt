package org.github.raspopov.services.platform

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


actual object CryptoPlatform {
    actual fun pbkdf2(
        password: ByteArray,
        salt: ByteArray,
        iterations: Int,
        keyLength: Int
    ): ByteArray {
        val spec = PBEKeySpec(
            password.toString(Charsets.UTF_8).toCharArray(),
            salt,
            iterations,
            keyLength * 8 // в битах
        )
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        return factory.generateSecret(spec).encoded
    }

    // Шифрование
    @Throws(Exception::class)
    actual fun encrypt(key: ByteArray, data: ByteArray): ByteArray {
        // Генерация случайного IV
        val iv = ByteArray(IV_LENGTH)
        val random = SecureRandom()
        random.nextBytes(iv)


        // Инициализация шифра
        val cipher = Cipher.getInstance(AES_ALGORITHM)
        val gcmSpec = GCMParameterSpec(TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), gcmSpec)


        // Шифрование
        val encrypted = cipher.doFinal(data)


        // Возвращаем IV + зашифрованные данные
        val result = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, result, 0, iv.size)
        System.arraycopy(encrypted, 0, result, iv.size, encrypted.size)

        return result
    }

    // Дешифрование
    @Throws(Exception::class)
    actual fun decrypt(key: ByteArray, encryptedData: ByteArray): ByteArray {
        try {
            // Извлекаем IV
            val iv = ByteArray(IV_LENGTH)
            System.arraycopy(encryptedData, 0, iv, 0, IV_LENGTH)


            // Извлекаем зашифрованные данные
            val cipherText = ByteArray(encryptedData.size - IV_LENGTH)
            System.arraycopy(encryptedData, IV_LENGTH, cipherText, 0, cipherText.size)


            // Инициализация шифра
            val cipher = Cipher.getInstance(AES_ALGORITHM)
            val gcmSpec = GCMParameterSpec(TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), gcmSpec)


            // Дешифрование
            return cipher.doFinal(cipherText)
        } catch (e: Exception) {
            // todo сделать логирование
//            throw RuntimeException("Decryption failed", e)
            return "Decryption failed".encodeToByteArray()
        }
    }

    actual fun randomBytes(array: ByteArray) {
        SecureRandom().nextBytes(array)
    }
}