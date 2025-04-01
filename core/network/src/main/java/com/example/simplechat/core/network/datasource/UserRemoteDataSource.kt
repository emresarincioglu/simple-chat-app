package com.example.simplechat.core.network.datasource

import com.example.simplechat.core.network.NetworkConstraints.COLLECTION_USERS
import com.example.simplechat.core.network.NetworkConstraints.FIELD_AVATAR_PATH
import com.example.simplechat.core.network.NetworkConstraints.FIELD_EMAIL
import com.example.simplechat.core.network.NetworkConstraints.FIELD_IS_ACTIVE
import com.example.simplechat.core.network.NetworkConstraints.FIELD_IV
import com.example.simplechat.core.network.NetworkConstraints.FIELD_PRIVATE_KEY
import com.example.simplechat.core.network.NetworkConstraints.FIELD_PUBLIC_KEY
import com.example.simplechat.core.network.NetworkConstraints.FIELD_USER_NAME
import com.example.simplechat.core.network.NetworkConstraints.FOLDER_AVATARS
import com.example.simplechat.core.network.awaitSuccessResult
import com.example.simplechat.core.network.model.CryptoKeysWithIv
import com.example.simplechat.core.network.model.NetworkUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    private val user get() = auth.currentUser!!

    fun getUserEmail() = auth.currentUser!!.email!!

    suspend fun getUserId() = db.collection(COLLECTION_USERS)
        .whereEqualTo(FIELD_IS_ACTIVE, true)
        .whereEqualTo(FIELD_EMAIL, user.email!!)
        .limit(1)
        .get().await()
        .documents[0]
        .reference
        .id

    suspend fun getUserCryptoData(userId: String): CryptoKeysWithIv {
        val userDoc = db.document("$COLLECTION_USERS/$userId").get().await()
        return CryptoKeysWithIv(
            iv = userDoc.getString(FIELD_IV)!!,
            privateKey = userDoc.getString(FIELD_PRIVATE_KEY)!!,
            publicKey = userDoc.getString(FIELD_PUBLIC_KEY)!!
        )
    }

    suspend fun getUser(userId: String): NetworkUser {
        val userDocument = db.document("$COLLECTION_USERS/$userId").get().await()
        val avatarUrl = userDocument.getString(FIELD_AVATAR_PATH)?.let {
            storage.getReferenceFromUrl(it).downloadUrl.await().toString()
        }

        return with(userDocument) {
            NetworkUser(
                name = getString(FIELD_USER_NAME)!!,
                email = getString(FIELD_EMAIL)!!,
                avatarUrl = avatarUrl,
                isActive = getBoolean(FIELD_IS_ACTIVE) ?: true
            )
        }
    }

    fun getUserStream(userId: String) = db.document("$COLLECTION_USERS/$userId")
        .snapshots()
        .distinctUntilChanged()
        .map { document ->
            val avatarUrl = document.getString(FIELD_AVATAR_PATH)?.let {
                storage.getReferenceFromUrl(it).downloadUrl.await().toString()
            }

            NetworkUser(
                name = document.getString(FIELD_USER_NAME)!!,
                email = document.getString(FIELD_EMAIL)!!,
                avatarUrl = avatarUrl,
                isActive = document.getBoolean(FIELD_IS_ACTIVE) ?: true
            )
        }

    suspend fun deleteUser(userId: String): Boolean {
        val userRef = db.document("$COLLECTION_USERS/$userId")
        val isSuccessful = user.delete().awaitSuccessResult()
        if (isSuccessful) {
            userRef.update(FIELD_IS_ACTIVE, false).await()
        }

        return isSuccessful
    }

    suspend fun setUserPassword(
        userId: String,
        password: String,
        privateKey: String,
        iv: String
    ): Boolean {
        val userRef = db.document("$COLLECTION_USERS/$userId")
        val isSuccessful = user.updatePassword(password).awaitSuccessResult()

        if (isSuccessful) {
            userRef.update(mapOf(FIELD_PRIVATE_KEY to privateKey, FIELD_IV to iv))
        }
        return isSuccessful
    }

    suspend fun setUserName(userId: String, name: String) = db.document("$COLLECTION_USERS/$userId")
        .set(mapOf(FIELD_USER_NAME to name), SetOptions.merge())
        .awaitSuccessResult()

    suspend fun setUserAvatar(
        userId: String, stream: InputStream?, fileExtension: String
    ): Boolean {
        val userRef = db.document("$COLLECTION_USERS/$userId")
        val oldPhotoPath = userRef.get().await().getString(FIELD_AVATAR_PATH)
        oldPhotoPath?.let { storage.getReferenceFromUrl(it).delete() }

        val newPhotoPath = stream?.let {
            val fileName = "${UUID.randomUUID()}.$fileExtension"
            val newPhotoPath = storage.reference.child(FOLDER_AVATARS).child(fileName)
            newPhotoPath.putStream(stream).await()
            newPhotoPath.toString()
        }

        return userRef.update(FIELD_AVATAR_PATH, newPhotoPath).awaitSuccessResult()
    }
}