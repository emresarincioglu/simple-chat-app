package com.example.simplechat.data.authentication.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.simplechat.data.authentication.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SyncRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val localUserId = inputData.getInt(WorkerConstraints.DATA_LOCAL_USER_ID, -1)
        if (localUserId == -1) { throw Exception("Local user id must be specified.") }
        val remoteUserId = inputData.getString(WorkerConstraints.DATA_REMOTE_USER_ID)!!

        return try {
            repository.syncFriends(localUserId, remoteUserId)
            repository.syncMessages(localUserId, remoteUserId)
            Result.success()
        } catch (ex: Exception) {
            Result.failure()
        }
    }
}