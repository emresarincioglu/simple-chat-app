package com.example.simplechat.core.crypto.cipher

import android.security.keystore.KeyProperties
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

internal object SymmetricCipher {

    const val KEY_SIZE = 256
    const val DIGEST = KeyProperties.DIGEST_SHA256
    const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
    const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE

    private const val IV_SIZE = 12
    private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"

    private val random = SecureRandom()

    fun encrypt(data: ByteArray, key: SecretKey, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(IV_SIZE * 8, iv))
        }

        return cipher.doFinal(data)
    }

    fun encrypt(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val keyHash = MessageDigest.getInstance(DIGEST).digest(key)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.ENCRYPT_MODE,
                SecretKeySpec(keyHash, ALGORITHM),
                GCMParameterSpec(IV_SIZE * 8, iv)
            )
        }

        return cipher.doFinal(data)
    }

    fun decrypt(data: ByteArray, key: SecretKey, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(IV_SIZE * 8, iv))
        }

        return cipher.doFinal(data)
    }

    fun decrypt(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val keyHash = MessageDigest.getInstance(DIGEST).digest(key)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(keyHash, ALGORITHM),
                GCMParameterSpec(IV_SIZE * 8, iv)
            )
        }

        return cipher.doFinal(data)
    }

    fun createIv(): ByteArray {
        val iv = ByteArray(IV_SIZE)
        random.nextBytes(iv)
        return iv
    }
}