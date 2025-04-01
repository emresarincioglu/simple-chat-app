package com.example.simplechat.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.example.simplechat.core.crypto.cipher.AsymmetricCipher
import com.example.simplechat.core.crypto.cipher.SymmetricCipher
import com.example.simplechat.core.datastore.DataStoreSource
import kotlinx.coroutines.flow.first
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyPair
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject

class CryptoManager @Inject constructor(private val dataSource: DataStoreSource) {

    companion object {
        private const val KEY_ALIAS = "secret_key"
        private const val KEY_STORE = "AndroidKeyStore"
    }

    private val keyStore = KeyStore.getInstance(KEY_STORE).apply { load(null) }

    suspend fun encryptMessage(message: String): String {
        val messageBytes = message.encodeToByteArray()
        val publicKey = dataSource.publicKeyStream.first()!!.decodeBase64()
        return AsymmetricCipher.encrypt(messageBytes, publicKey).encodeToBase64()
    }

    fun encryptMessage(message: String, publicKey: String): String {
        val messageBytes = message.encodeToByteArray()
        return AsymmetricCipher.encrypt(messageBytes, publicKey.decodeBase64()).encodeToBase64()
    }

    suspend fun decryptMessage(message: String): String {
        val iv = dataSource.ivStream.first()!!.decodeBase64()
        val encryptedPrivateKey = dataSource.privateKeyStream.first()!!.decodeBase64()
        val privateKey = SymmetricCipher.decrypt(encryptedPrivateKey, getSecretKey(), iv)

        val decodedMessage = message.decodeBase64()
        val decryptedMessage = AsymmetricCipher.decrypt(decodedMessage, privateKey)
        return String(decryptedMessage)
    }

    suspend fun encryptFile(inStream: InputStream, outStream: OutputStream) {
        val publicKey = dataSource.publicKeyStream.first()!!.decodeBase64()
        AsymmetricCipher.encryptFile(inStream, outStream, publicKey)
    }

    suspend fun encryptFile(inStream: InputStream, outStream: OutputStream, publicKey: String) {
        AsymmetricCipher.encryptFile(inStream, outStream, publicKey.decodeBase64())
    }

    suspend fun decryptFile(inStream: InputStream, outStream: OutputStream) {
        val iv = dataSource.ivStream.first()!!.decodeBase64()
        val encryptedPrivateKey = dataSource.privateKeyStream.first()!!.decodeBase64()

        val privateKey = SymmetricCipher.decrypt(encryptedPrivateKey, getSecretKey(), iv)
        AsymmetricCipher.decryptFile(inStream, outStream, privateKey)
    }

    /**
     * @return First element is iv, second element is encrypted private key
     */
    suspend fun getEncryptedPrivateKey(secretKey: String): Pair<String, String> {
        val privateKey = decryptPrivateKey(dataSource.privateKeyStream.first()!!)
        return encryptPrivateKey(privateKey, secretKey)
    }

    /**
     * @return First element is iv, second element is encrypted private key
     */
    fun encryptPrivateKey(privateKey: ByteArray, secretKey: String): Pair<String, String> {
        val iv = SymmetricCipher.createIv()
        val encryptedPrivateKey = SymmetricCipher.encrypt(
            data = privateKey, key = secretKey.encodeToByteArray(), iv = iv
        )
        return iv.encodeToBase64() to encryptedPrivateKey.encodeToBase64()
    }

    private suspend fun encryptPrivateKey(privateKey: ByteArray): String {
        return SymmetricCipher.encrypt(privateKey, getSecretKey(), getIv()).encodeToBase64()
    }

    fun decryptPrivateKey(privateKey: String, secretKey: String, iv: String): String {
        return SymmetricCipher.decrypt(
            data = privateKey.decodeBase64(),
            key = secretKey.encodeToByteArray(),
            iv = iv.decodeBase64()
        ).encodeToBase64()
    }

    private suspend fun decryptPrivateKey(privateKey: String) =
        SymmetricCipher.decrypt(privateKey.decodeBase64(), getSecretKey(), getIv())

    suspend fun createCryptoKeys(): KeyPair {
        val keyPair = AsymmetricCipher.createKeyPair()

        dataSource.setPublicKey(keyPair.public.encoded.encodeToBase64())
        dataSource.setPrivateKey(encryptPrivateKey(keyPair.private.encoded))

        return keyPair
    }

    suspend fun setCryptoKeys(privateKey: String, publicKey: String) {
        dataSource.setPublicKey(publicKey)
        dataSource.setPrivateKey(encryptPrivateKey(privateKey.decodeBase64()))
    }

    suspend fun deleteData() = dataSource.deleteData()

    private fun getSecretKey(): SecretKey {
        val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return entry?.secretKey ?: createSecretKey()
    }

    private fun createSecretKey(): SecretKey {
        val keyGenSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(SymmetricCipher.BLOCK_MODE)
            .setEncryptionPaddings(SymmetricCipher.PADDING)
            .setKeySize(SymmetricCipher.KEY_SIZE)
            .setDigests(SymmetricCipher.DIGEST)
            .setUserAuthenticationRequired(false)
            .setRandomizedEncryptionRequired(false)
            .build()

        return KeyGenerator.getInstance(SymmetricCipher.ALGORITHM, KEY_STORE).apply {
            init(keyGenSpec)
        }.generateKey()
    }

    private suspend fun getIv(): ByteArray {
        var iv = dataSource.ivStream.first()?.decodeBase64()
        if (iv == null) {
            iv = SymmetricCipher.createIv()
            dataSource.setIv(iv.encodeToBase64())
        }

        return iv
    }
}