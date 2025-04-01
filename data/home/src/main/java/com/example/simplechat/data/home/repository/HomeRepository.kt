package com.example.simplechat.data.home.repository

import android.graphics.Bitmap
import android.net.Uri
import androidx.paging.PagingSource
import com.example.simplechat.core.common.model.Friend
import com.example.simplechat.core.common.model.message.Message
import kotlinx.coroutines.flow.Flow
import java.io.FileInputStream
import java.io.FileOutputStream

interface HomeRepository {
    fun getFriendStream(remoteUserId: String, friendId: Int): Flow<Friend?>

    fun getFriendsWithLastMessageStream(
        localUserId: Int, remoteUserId: String
    ): Flow<List<Pair<Friend, Message?>>>

    fun getFriendNewMessagesStream(
        remoteUserId: String, friendId: Int, onOverflow: () -> Unit, maxSize: Int
    ): Flow<List<Message>>

    fun getFriendPagedMessages(friendId: Int): PagingSource<Int, Message>

    fun getUserFriendCodeQr(remoteUserId: String): Bitmap

    suspend fun addFriend(remoteUserId: String, friendCode: String): Boolean

    suspend fun deleteFriend(remoteUserId: String, friendId: Int): Boolean

    suspend fun sendMessage(remoteUserId: String, friendId: Int, message: String): Boolean

    suspend fun sendImageMessage(remoteUserId: String, friendId: Int, image: Uri)

    fun clearCache()

    suspend fun _encryptImage(
        inStream: FileInputStream, outStream: FileOutputStream, publicKey: String? = null
    )

    suspend fun _sendImage(
        remoteUserId: String,
        friendRecordId: String,
        recordPathOfUser: String,
        userEncryptedImageStream: FileInputStream,
        friendEncryptedImageStream: FileInputStream,
        fileExtension: String
    ): Boolean
}