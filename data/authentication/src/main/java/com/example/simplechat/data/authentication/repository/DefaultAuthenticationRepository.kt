package com.example.simplechat.data.authentication.repository

import com.example.simplechat.core.common.di.DefaultDispatcher
import com.example.simplechat.core.common.di.IoDispatcher
import com.example.simplechat.core.crypto.CryptoManager
import com.example.simplechat.core.crypto.encodeToBase64
import com.example.simplechat.core.database.datasource.UserLocalDataSource
import com.example.simplechat.core.network.datasource.AuthenticationRemoteDataSource
import com.example.simplechat.core.network.datasource.UserRemoteDataSource
import com.example.simplechat.core.network.model.CryptoKeysWithIv
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultAuthenticationRepository @Inject constructor(
    private val cryptoManager: CryptoManager,
    private val localUser: UserLocalDataSource,
    private val remoteUser: UserRemoteDataSource,
    private val remoteAuth: AuthenticationRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : AuthenticationRepository {

    override var localUserId: Int? = null
        private set

    override var remoteUserId: String? = null
        private set

    override val isLoggedIn get() = remoteAuth.isLoggedIn

    override suspend fun initialize() = coroutineScope {
        if (isLoggedIn) {
            val deferredRemoteUserId = async(ioDispatcher) { remoteUser.getUserId() }
            val deferredLocalUserId = async(ioDispatcher) {
                val userEmail = remoteUser.getUserEmail()
                localUser.getUser(userEmail)!!.uid
            }

            remoteUserId = deferredRemoteUserId.await()
            localUserId = deferredLocalUserId.await()
        }
    }

    override suspend fun logIn(email: String, password: String) = withContext(ioDispatcher) {
        val isSuccessful = remoteAuth.logIn(email, password)

        if (isSuccessful) {
            remoteUserId = remoteUser.getUserId()
            val cryptoData = remoteUser.getUserCryptoData(remoteUserId!!)

            withContext(defaultDispatcher) {
                val decryptedPrivateKey = cryptoManager.decryptPrivateKey(
                    privateKey = cryptoData.privateKey, secretKey = password, iv = cryptoData.iv
                )
                cryptoManager.setCryptoKeys(
                    privateKey = decryptedPrivateKey, publicKey = cryptoData.publicKey
                )
            }

            localUserId = localUser.getUser(email)?.uid ?: remoteUser.getUser(remoteUserId!!).run {
                localUser.addUser(name, email, avatarUrl).toInt()
            }
        }
        isSuccessful
    }

    override suspend fun signUp(name: String, email: String, password: String): Boolean {
        val cryptoData = withContext(defaultDispatcher) {
            val keyPair = cryptoManager.createCryptoKeys()
            val (iv, privateKey) = cryptoManager.encryptPrivateKey(
                privateKey = keyPair.private.encoded, secretKey = password
            )
            CryptoKeysWithIv(iv, privateKey, keyPair.public.encoded.encodeToBase64())
        }

        return withContext(ioDispatcher) {
            remoteUserId = remoteAuth.signUp(
                name = name,
                email = email,
                password = password,
                publicKey = cryptoData.publicKey,
                privateKey = cryptoData.privateKey,
                iv = cryptoData.iv
            )

            val isSuccessful = remoteUserId != null
            if (isSuccessful) {
                localUserId = localUser.addUser(name = name, email = email).toInt()
            }

            isSuccessful
        }
    }

    override suspend fun sendPasswordResetEmail(email: String) = withContext(ioDispatcher) {
        remoteAuth.sendPasswordResetEmail(email)
    }

    override fun clearCache() {
        localUserId = null
        remoteUserId = null
    }
}