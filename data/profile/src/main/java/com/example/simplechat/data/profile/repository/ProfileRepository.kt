package com.example.simplechat.data.profile.repository

import android.net.Uri
import com.example.simplechat.core.common.model.User
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

interface ProfileRepository {
    fun getUserStream(localUserId: Int, remoteUserId: String): Flow<User?>
    suspend fun setUserName(remoteUserId: String, name: String): Boolean
    suspend fun setPassword(remoteUserId: String, password: String): Boolean
    suspend fun setAvatar(remoteUserId: String, avatar: Uri?)
    suspend fun logOut()
    suspend fun deleteUser(localUserId: Int, remoteUserId: String): Boolean
    fun isViolenceImage(uri: Uri): Boolean
    suspend fun _uploadAvatar(userId: String, stream: InputStream?, fileExtension: String): Boolean
}