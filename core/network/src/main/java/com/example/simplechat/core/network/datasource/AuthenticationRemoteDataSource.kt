package com.example.simplechat.core.network.datasource

import com.example.simplechat.core.network.NetworkConstraints.COLLECTION_USERS
import com.example.simplechat.core.network.NetworkConstraints.FIELD_EMAIL
import com.example.simplechat.core.network.NetworkConstraints.FIELD_IS_ACTIVE
import com.example.simplechat.core.network.NetworkConstraints.FIELD_IV
import com.example.simplechat.core.network.NetworkConstraints.FIELD_PRIVATE_KEY
import com.example.simplechat.core.network.NetworkConstraints.FIELD_PUBLIC_KEY
import com.example.simplechat.core.network.NetworkConstraints.FIELD_USER_NAME
import com.example.simplechat.core.network.awaitSuccessResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthenticationRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth, private val db: FirebaseFirestore
) {
    val isLoggedIn get() = auth.currentUser != null

    suspend fun logIn(email: String, password: String): Boolean {
        return auth.signInWithEmailAndPassword(email, password).await().user != null
    }

    /**
     * @return Remote database user id
     */
    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        publicKey: String,
        privateKey: String,
        iv: String
    ): String? {
        auth.createUserWithEmailAndPassword(email, password).await().user ?: return null

        val userRef = db.collection(COLLECTION_USERS).add(
            mapOf(
                FIELD_EMAIL to email,
                FIELD_USER_NAME to name,
                FIELD_PUBLIC_KEY to publicKey,
                FIELD_PRIVATE_KEY to privateKey,
                FIELD_IV to iv,
                FIELD_IS_ACTIVE to true
            )
        ).await()

        return userRef.id
    }

    fun logOut() = auth.signOut()

    suspend fun sendPasswordResetEmail(email: String): Boolean {
        return auth.sendPasswordResetEmail(email).awaitSuccessResult()
    }
}