package com.example.simplechat.core.crypto.cipher

import android.security.keystore.KeyProperties
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

internal object AsymmetricCipher {

    private const val KEY_SIZE = 2048
    private const val BUFFER_SIZE = 245
    private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_RSA
    private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_ECB
    private const val PADDING = KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1
    private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"

    fun encrypt(data: ByteArray, publicKeyBytes: ByteArray): ByteArray {
        val keySpec = X509EncodedKeySpec(publicKeyBytes)
        val publicKey = KeyFactory.getInstance(ALGORITHM).generatePublic(keySpec)

        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, publicKey)
        }

        return cipher.doFinal(data)
    }

    fun decrypt(data: ByteArray, privateKeyBytes: ByteArray): ByteArray {
        val keySpec = PKCS8EncodedKeySpec(privateKeyBytes)
        val privateKey = KeyFactory.getInstance(ALGORITHM).generatePrivate(keySpec)

        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, privateKey)
        }

        return cipher.doFinal(data)
    }

    suspend fun encryptFile(
        inStream: InputStream, outStream: OutputStream, publicKeyBytes: ByteArray
    ) {
        val keySpec = X509EncodedKeySpec(publicKeyBytes)
        val publicKey = KeyFactory.getInstance(ALGORITHM).generatePublic(keySpec)

        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, publicKey)
        }

        val buffer = ByteArray(BUFFER_SIZE)
        var readBytes = inStream.read(buffer)
        while (readBytes > -1) {
            val encryptedBytes = cipher.doFinal(buffer)
            outStream.write(encryptedBytes, 0, encryptedBytes.size)
            readBytes = inStream.read(buffer)
        }
    }

    suspend fun decryptFile(
        inStream: InputStream, outStream: OutputStream, privateKeyBytes: ByteArray
    ) {
        val keySpec = PKCS8EncodedKeySpec(privateKeyBytes)
        val privateKey = KeyFactory.getInstance(ALGORITHM).generatePrivate(keySpec)

        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, privateKey)
        }

        val buffer = ByteArray(KEY_SIZE / 8)
        var readBytes = inStream.read(buffer)
        while (readBytes > -1) {
            val decryptedBytes = cipher.doFinal(buffer)
            outStream.write(decryptedBytes, 0, decryptedBytes.size)
            readBytes = inStream.read(buffer)
        }
    }

    fun createKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance(ALGORITHM).apply { initialize(KEY_SIZE) }
        return generator.generateKeyPair()
    }
}