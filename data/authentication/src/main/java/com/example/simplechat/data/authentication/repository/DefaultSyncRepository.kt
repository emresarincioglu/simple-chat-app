package com.example.simplechat.data.authentication.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.simplechat.core.common.di.IoDispatcher
import com.example.simplechat.core.common.model.message.ImageMessage
import com.example.simplechat.core.common.model.message.TextMessage
import com.example.simplechat.core.crypto.CryptoManager
import com.example.simplechat.core.database.datasource.FriendLocalDataSource
import com.example.simplechat.core.database.datasource.MessageLocalDataSource
import com.example.simplechat.core.database.entity.FriendEntity
import com.example.simplechat.core.database.entity.MessageEntity
import com.example.simplechat.core.network.datasource.FriendRemoteDataSource
import com.example.simplechat.core.network.datasource.MessageRemoteDataSource
import com.example.simplechat.data.authentication.worker.DataSyncWorker
import com.example.simplechat.data.authentication.worker.WorkerConstraints.DATA_LOCAL_USER_ID
import com.example.simplechat.data.authentication.worker.WorkerConstraints.DATA_REMOTE_USER_ID
import com.example.simplechat.data.authentication.worker.WorkerConstraints.WORK_NAME_SYNC_DATA
import com.example.simplechat.data.authentication.worker.WorkerConstraints.WORK_SYNC_DATA_INTERVAL
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DefaultSyncRepository @Inject constructor(
    private val cryptoManager: CryptoManager,
    private val remoteFriend: FriendRemoteDataSource,
    private val localFriend: FriendLocalDataSource,
    private val remoteMessage: MessageRemoteDataSource,
    private val localMessage: MessageLocalDataSource,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SyncRepository {

    override suspend fun syncFriends(localUserId: Int, remoteUserId: String) {
        withContext(ioDispatcher) {
            val friends = remoteFriend.getFriends(remoteUserId).groupBy { it.isActive }

            friends[true]?.let { activeFriends ->
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

            friends[false]?.let { inactiveFriends ->
                for (friend in inactiveFriends) {
                    localFriend.deleteFriend(localUserId, friend.recordId)
                }
            }
        }
    }

    override suspend fun syncMessages(localUserId: Int, remoteUserId: String) {
        withContext(ioDispatcher) {
            val friends = localFriend.getFriends(localUserId)
            for (friend in friends) {
                val remoteSyncTime = remoteMessage.getLastMessageTime(
                    remoteUserId, friend.remoteRecordId
                ) ?: continue
                var localSyncTime = localMessage.getLastMessageTime(friend.uid) ?: 0

                while (localSyncTime < remoteSyncTime) {
                    val messages = remoteMessage.getMessagesAfter(
                        userId = remoteUserId,
                        friendRecordId = friend.remoteRecordId,
                        time = localSyncTime,
                        count = 20
                    )

                    localMessage.addMessages(
                        messages.map { message ->
                            when (message) {
                                is TextMessage -> MessageEntity(
                                    friendId = friend.uid,
                                    text = cryptoManager.decryptMessage(message.text),
                                    image = null,
                                    time = message.time,
                                    isFromUser = message.isFromUser
                                )

                                is ImageMessage -> MessageEntity(
                                    friendId = friend.uid,
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
    }

    override fun enqueuePeriodicSync(localUserId: Int, remoteUserId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresDeviceIdle(true)
            .build()

        val inputData = Data.Builder()
            .putInt(DATA_LOCAL_USER_ID, localUserId)
            .putString(DATA_REMOTE_USER_ID, remoteUserId)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_SYNC_DATA,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<DataSyncWorker>(WORK_SYNC_DATA_INTERVAL, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        )
    }

    override fun cancelPeriodicSync() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_SYNC_DATA)
    }
}