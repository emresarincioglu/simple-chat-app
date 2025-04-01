package com.example.simplechat.data.authentication.repository

interface SyncRepository {
    suspend fun syncFriends(localUserId: Int, remoteUserId: String)
    suspend fun syncMessages(localUserId: Int, remoteUserId: String)
    fun enqueuePeriodicSync(localUserId: Int, remoteUserId: String)
    fun cancelPeriodicSync()
}