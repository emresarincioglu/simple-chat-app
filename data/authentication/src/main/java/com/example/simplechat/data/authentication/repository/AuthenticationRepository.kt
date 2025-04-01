package com.example.simplechat.data.authentication.repository

interface AuthenticationRepository {
    val localUserId: Int?
    val remoteUserId: String?
    val isLoggedIn: Boolean
    suspend fun initialize()
    suspend fun logIn(email: String, password: String): Boolean
    suspend fun signUp(name: String, email: String, password: String): Boolean
    suspend fun sendPasswordResetEmail(email: String): Boolean
    fun clearCache()
}