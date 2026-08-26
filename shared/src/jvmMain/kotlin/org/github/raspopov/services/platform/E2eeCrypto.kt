package org.github.raspopov.services.platform

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec

import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Класс для генерации E2EE ключей (асимметричное шифрование)
 * Использует криптографию на эллиптических кривых (ECDH)
 * Совместим с Kotlin Multiplatform Android и Desktop
 */
actual object E2eeCrypto {

    /**
     * Генерирует пару ключей (приватный и публичный)
     * @return KeyPair содержащий приватный и публичный ключи
     */
    actual fun generateKeyPair(): KeyPairWrapper {
        try {
            val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM)
            val ecSpec = ECGenParameterSpec(CURVE)
            val secureRandom: SecureRandom = SecureRandom()

            keyPairGenerator.initialize(ecSpec, secureRandom)
            val keyPair = keyPairGenerator.generateKeyPair()

            return KeyPairWrapper(
                keyPair.private.encoded,
                keyPair.public.encoded,
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to generate E2EE key pair", e)
        }
    }

    /**
     * Шифрование данных для собеседника
     * Использует ваш приватный ключ + публичный ключ собеседника
     */
    actual fun encrypt(
        myPrivateKey: ByteArray,
        myPublicKey: ByteArray,
        otherPublicKey: ByteArray,
        data: ByteArray
    ): ByteArray {
        try {
            // 1. Генерируем общий секрет через ECDH
            val sharedSecret = generateSharedSecret(myPrivateKey, otherPublicKey)

            // 2. Создаем AES ключ из общего секрета
            val aesKey = deriveAESKey(sharedSecret)

            // 3. Шифруем данные AES/GCM
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(aesKey, "AES"))
            val iv = cipher.iv
            val encryptedData = cipher.doFinal(data)

            // 4. Возвращаем IV + зашифрованные данные
            return ByteArrayOutputStream().apply {
                write(intToBytes(iv.size))
                write(iv)
                write(encryptedData)
            }.toByteArray()

        } catch (e: Exception) {
            // todo сделать логирование
            throw RuntimeException("Encryption failed", e)
        }
    }

    /**
     * Дешифрование данных от собеседника
     */
    actual fun decrypt(
        myPrivateKey: ByteArray,
        myPublicKey: ByteArray,
        otherPublicKey: ByteArray,
        encryptedData: ByteArray
    ): ByteArray {
        try {
            // 1. Извлекаем IV и данные
            val (iv, ciphertext) = extractIVAndData(encryptedData)

            // 2. Генерируем тот же общий секрет
            // (Ваш приватный + публичный собеседника)
            val sharedSecret = generateSharedSecret(myPrivateKey, otherPublicKey)

            // 3. Создаем AES ключ
            val aesKey = deriveAESKey(sharedSecret)

            // 4. Расшифровываем
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(aesKey, "AES"),
                GCMParameterSpec(128, iv)
            )
            return cipher.doFinal(ciphertext)

        } catch (e: Exception) {
            // todo сделать логирование
//            throw RuntimeException("Decryption failed", e)
            return "Decryption failed".encodeToByteArray()
        }
    }

    private fun generateSharedSecret(privateKey: ByteArray, publicKey: ByteArray): ByteArray {
        val keyFactory = KeyFactory.getInstance("EC")
        val privateSpec = PKCS8EncodedKeySpec(privateKey)
        val privateKeyObj = keyFactory.generatePrivate(privateSpec)

        val publicSpec = X509EncodedKeySpec(publicKey)
        val publicKeyObj = keyFactory.generatePublic(publicSpec)

        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(privateKeyObj)
        keyAgreement.doPhase(publicKeyObj, true)
        return keyAgreement.generateSecret()
    }

    private fun deriveAESKey(sharedSecret: ByteArray): ByteArray {
        // Берем первые 32 байта для AES-256
        return if (sharedSecret.size >= 32) {
            sharedSecret.copyOf(32)
        } else {
            // Если секрет короче, дополняем нулями
            ByteArray(32).apply {
                System.arraycopy(sharedSecret, 0, this, 0, sharedSecret.size)
            }
        }
    }

    private fun intToBytes(value: Int): ByteArray {
        return ByteBuffer.allocate(4).putInt(value).array()
    }

    private fun extractIVAndData(data: ByteArray): Pair<ByteArray, ByteArray> {
        val buffer = ByteBuffer.wrap(data)
        val ivSize = buffer.int
        val iv = ByteArray(ivSize)
        buffer.get(iv)
        val ciphertext = ByteArray(buffer.remaining())
        buffer.get(ciphertext)
        return Pair(iv, ciphertext)
    }
}