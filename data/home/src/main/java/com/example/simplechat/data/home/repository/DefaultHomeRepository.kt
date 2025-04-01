package com.example.simplechat.data.home.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.net.toUri
import androidx.paging.PagingSource
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.simplechat.core.common.di.DefaultDispatcher
import com.example.simplechat.core.common.di.IoDispatcher
import com.example.simplechat.core.common.model.Friend
import com.example.simplechat.core.common.model.message.ImageMessage
import com.example.simplechat.core.common.model.message.Message
import com.example.simplechat.core.common.model.message.TextMessage
import com.example.simplechat.core.crypto.CryptoManager
import com.example.simplechat.core.database.datasource.FriendLocalDataSource
import com.example.simplechat.core.database.datasource.MessageLocalDataSource
import com.example.simplechat.core.database.entity.FriendEntity
import com.example.simplechat.core.database.entity.MessageEntity
import com.example.simplechat.core.network.datasource.FriendRemoteDataSource
import com.example.simplechat.core.network.datasource.MessageRemoteDataSource
import com.example.simplechat.data.home.MessagePagingSource
import com.example.simplechat.data.home.worker.CompressImageWorker
import com.example.simplechat.data.home.worker.EncryptImageWorker
import com.example.simplechat.data.home.worker.SendImageWorker
import com.example.simplechat.data.home.worker.WorkerConstraints.DATA_FRIEND_PUBLIC_KEY
import com.example.simplechat.data.home.worker.WorkerConstraints.DATA_FRIEND_RECORD_ID
import com.example.simplechat.data.home.worker.WorkerConstraints.DATA_IMAGE_URI
import com.example.simplechat.data.home.worker.WorkerConstraints.DATA_RECORD_PATH_OF_USER
import com.example.simplechat.data.home.worker.WorkerConstraints.DATA_USER_ID
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DefaultHomeRepository @Inject constructor(
    private val localMessage: MessageLocalDataSource,
    private val remoteMessage: MessageRemoteDataSource,
    private val localFriend: FriendLocalDataSource,
    private val remoteFriend: FriendRemoteDataSource,
    private val cryptoManager: CryptoManager,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : HomeRepository {

    companion object {
        private const val QR_SIZE = 512
    }

    private val recordPathsOfUser = mutableMapOf<Int, String>()

    override fun getFriendStream(remoteUserId: String, friendId: Int) = channelFlow {
        launch(ioDispatcher) {
            val recordId = localFriend.getFriend(friendId).remoteRecordId
            remoteFriend.getFriendStream(remoteUserId, recordId).collect {
                if (it.isActive) {
                    localFriend.updateFriend(friendId, it.name, it.avatarUrl)
                } else {
                    localFriend.deleteFriend(friendId)
                }
            }
        }

        localFriend.getFriendStream(friendId)
            .flowOn(ioDispatcher)
            .map { friend ->
                friend?.let { Friend(it.uid, it.name, it.avatar) }
            }
            .collect(::send)
    }

    override fun getFriendsWithLastMessageStream(
        localUserId: Int, remoteUserId: String
    ) = channelFlow {
        launch(ioDispatcher) {
            remoteFriend.getFriendsChangeStream(remoteUserId).collect { friends ->
                val groupedFriends = friends.groupBy { it.isActive }

                groupedFriends[true]?.let { activeFriends ->
                    localFriend.addFriends(
                        localUserId,
                        activeFriends.map { friend ->
                            FriendEntity(
                                userId = localUserId,
                                name = friend.name,
                                avatar = friend.avatarUrl,
                                publicKey = friend.publicKey,
                                remoteRecordId = friend.recordId
                            )
                        }
                    )
                }

                groupedFriends[false]?.let { inactiveFriends ->
                    for (friend in inactiveFriends) {
                        localFriend.deleteFriend(localUserId, friend.recordId)
                    }
                }
            }
        }

        localFriend.getFriendsWithLastMessageStream(localUserId)
            .flowOn(ioDispatcher)
            .map { friends ->
                friends.map { (friend, message) ->
                    val friend = Friend(id = friend.uid, name = friend.name, avatar = friend.avatar)
                    val lastMessage = message?.let {
                        if (it.image == null) {
                            TextMessage(
                                text = it.text!!, time = it.time, isFromUser = it.isFromUser
                            )
                        } else {
                            ImageMessage(
                                imageUrl = it.image!!.toUri(),
                                time = it.time,
                                isFromUser = it.isFromUser
                            )
                        }
                    }

                    friend to lastMessage
                }
            }
            .collect(::send)
    }

    override fun getFriendNewMessagesStream(
        remoteUserId: String, friendId: Int, onOverflow: () -> Unit, maxSize: Int
    ) = channelFlow {
        var localSyncTime = localMessage.getLastMessageTime(friendId) ?: 0
        val friendRecordId = localFriend.getFriend(friendId).remoteRecordId

        launch(ioDispatcher) {
            remoteMessage.getLastMessageTimeStream(remoteUserId, friendRecordId)
                .filterNotNull()
                .collectLatest { remoteSyncTime ->
                    while (localSyncTime < remoteSyncTime) {
                        val messages = remoteMessage.getMessagesAfter(
                            userId = remoteUserId,
                            friendRecordId = friendRecordId,
                            time = localSyncTime,
                            count = 20
                        )

                        localMessage.addMessages(
                            messages.map { message ->
                                when (message) {
                                    is TextMessage -> MessageEntity(
                                        friendId = friendId,
                                        text = cryptoManager.decryptMessage(message.text),
                                        image = null,
                                        time = message.time,
                                        isFromUser = message.isFromUser
                                    )

                                    is ImageMessage -> MessageEntity(
                                        friendId = friendId,
                                        text = null,
                                        image = message.imageUrl.toString(),
                                        time = message.time,
                                        isFromUser = message.isFromUser
                                    )

                                    else -> throw Exception("Unhandled message type")
                                }
                            }
                        )

                        localSyncTime = messages[messages.lastIndex].time
                    }
                }
        }

        var localStreamJob: Job? = null
        while (true) {
            ensureActive()
            localStreamJob = launch {
                localMessage.getMessagesAfterStream(friendId, localSyncTime)
                    .flowOn(ioDispatcher)
                    .onEach { messages ->
                        if (messages.size >= maxSize) {
                            onOverflow()
                            localStreamJob?.cancel()
                        }
                    }
                    .map { messages ->
                        messages.map {
                            if (it.image == null) {
                                TextMessage(
                                    text = it.text!!, time = it.time, isFromUser = it.isFromUser
                                )
                            } else {
                                ImageMessage(
                                    imageUrl = it.image!!.toUri(),
                                    time = it.time,
                                    isFromUser = it.isFromUser
                                )
                            }
                        }
                    }
                    .collect(::send)
            }
            localStreamJob.join()
        }
    }

    override fun getFriendPagedMessages(friendId: Int): PagingSource<Int, Message> {
        return MessagePagingSource(friendId, localMessage)
    }

    override fun getUserFriendCodeQr(remoteUserId: String): Bitmap {
        val qrContent = "android-app://com.example.simplechat/share/$remoteUserId"
        val bitMatrix = QRCodeWriter().encode(
            qrContent, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, mapOf(EncodeHintType.MARGIN to 1)
        )

        return createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.RGB_565).also { bitmap ->
            for (x in 0 until QR_SIZE) {
                for (y in 0 until QR_SIZE) {
                    bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
        }
    }

    override suspend fun addFriend(remoteUserId: String, friendCode: String): Boolean {
        if (friendCode == remoteUserId) return false
        return withContext(ioDispatcher) {
            remoteFriend.addFriend(remoteUserId, friendCode)
        }
    }

    override suspend fun deleteFriend(remoteUserId: String, friendId: Int): Boolean {
        return withContext(ioDispatcher) {
            val friendRecordId = localFriend.getFriend(friendId).remoteRecordId
            remoteFriend.deleteFriend(remoteUserId, friendRecordId)
        }
    }

    override suspend fun sendMessage(
        remoteUserId: String, friendId: Int, message: String
    ) = coroutineScope {
        // TODO: Cache local friend
        val friend = withContext(ioDispatcher) { localFriend.getFriend(friendId) }
        val recordPathOfUser = recordPathsOfUser[friendId]
            ?.let { CompletableDeferred(it) }
            ?: async(ioDispatcher) {
                val recordPath = remoteFriend.getRecordPathOfUserFromFriend(
                    userId = remoteUserId, friendRecordId = friend.remoteRecordId
                )
                recordPathsOfUser[friendId] = recordPath
                recordPath
            }

        val userEncryptedMessage = async(defaultDispatcher) {
            cryptoManager.encryptMessage(message)
        }
        val friendEncryptedMessage = async(defaultDispatcher) {
            cryptoManager.encryptMessage(message, friend.publicKey)
        }

        withContext(ioDispatcher) {
            remoteMessage.sendMessage(
                userId = remoteUserId,
                friendRecordId = friend.remoteRecordId,
                recordPathOfUser = recordPathOfUser.await(),
                userEncryptedMessage = userEncryptedMessage.await(),
                friendEncryptedMessage = friendEncryptedMessage.await()
            )
        }
    }

    override suspend fun sendImageMessage(remoteUserId: String, friendId: Int, image: Uri) {
        // TODO: Cache local friend
        val friend = withContext(ioDispatcher) { localFriend.getFriend(friendId) }
        val recordPathOfUser = recordPathsOfUser[friendId] ?: withContext(ioDispatcher) {
            val recordPath = remoteFriend.getRecordPathOfUserFromFriend(
                userId = remoteUserId, friendRecordId = friend.remoteRecordId
            )
            recordPathsOfUser[friendId] = recordPath
            recordPath
        }

        WorkManager.getInstance(context)
            .beginWith(
                OneTimeWorkRequestBuilder<CompressImageWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        WorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS
                    )
                    .setInputData(
                        Data.Builder()
                            .putString(DATA_IMAGE_URI, image.toString())
                            .build()
                    )
                    .build()
            )
            .then(
                OneTimeWorkRequestBuilder<EncryptImageWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setInputData(
                        Data.Builder()
                            .putString(DATA_FRIEND_PUBLIC_KEY, friend.publicKey)
                            .build()
                    )
                    .build()
            )
            .then(
                OneTimeWorkRequestBuilder<SendImageWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setInputData(
                        Data.Builder()
                            .putString(DATA_USER_ID, remoteUserId)
                            .putString(DATA_FRIEND_RECORD_ID, friend.remoteRecordId)
                            .putString(DATA_RECORD_PATH_OF_USER, recordPathOfUser)
                            .build()
                    )
                    .build()
            )
            .enqueue()
    }

    override fun clearCache() {
        recordPathsOfUser.clear()
    }

    /**
     * Should not be used in ViewModels
     */
    override suspend fun _encryptImage(
        inStream: FileInputStream, outStream: FileOutputStream, publicKey: String?
    ) = withContext(ioDispatcher) {
        if (publicKey == null) {
            cryptoManager.encryptFile(inStream, outStream)
        } else {
            cryptoManager.encryptFile(inStream, outStream, publicKey)
        }
    }

    /**
     * Should not be used in ViewModels
     */
    override suspend fun _sendImage(
        remoteUserId: String,
        friendRecordId: String,
        recordPathOfUser: String,
        userEncryptedImageStream: FileInputStream,
        friendEncryptedImageStream: FileInputStream,
        fileExtension: String
    ) = withContext(ioDispatcher) {
        remoteMessage.sendImageMessage(
            remoteUserId,
            friendRecordId,
            recordPathOfUser,
            userEncryptedImageStream,
            friendEncryptedImageStream,
            fileExtension
        )
    }
}