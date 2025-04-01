package com.example.simplechat.data.profile.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.simplechat.data.profile.repository.ProfileRepository
import com.example.simplechat.data.profile.worker.WorkerConstraints.DATA_COMPRESSED_IMAGE_FILE_NAME
import com.example.simplechat.data.profile.worker.WorkerConstraints.DATA_USER_ID
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import com.example.simplechat.core.ui.R as coreUiR

@HiltWorker
class UploadAvatarWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ProfileRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val NOTIFICATION_ID = 1
    }

    override suspend fun doWork(): Result {
        val userId = inputData.getString(DATA_USER_ID)!!
        val compressedImageFile = File(
            applicationContext.cacheDir,
            inputData.getString(DATA_COMPRESSED_IMAGE_FILE_NAME)!!
        )

        return try {
            if (!compressedImageFile.exists()) {
                return Result.failure()
            }

            compressedImageFile.inputStream().use { inStream ->
                repository._uploadAvatar(userId, inStream, compressedImageFile.extension)
            }
            compressedImageFile.delete()

            Result.success()
        } catch (ex: Exception) {
            Result.retry()
        }
    }

    override suspend fun getForegroundInfo() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ForegroundInfo(
            NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    } else {
        ForegroundInfo(NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        return NotificationCompat.Builder(
            applicationContext, applicationContext.getString(coreUiR.string.notif_chan_worker_id)
        )
            .setSmallIcon(coreUiR.mipmap.ic_launcher)
            .setContentTitle(applicationContext.getString(coreUiR.string.app_name))
            .setContentText(applicationContext.getString(coreUiR.string.notif_upload_image_worker_content))
            .setOngoing(true)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        NotificationManagerCompat.from(applicationContext).createNotificationChannel(
            NotificationChannel(
                applicationContext.getString(coreUiR.string.notif_chan_worker_id),
                applicationContext.getString(coreUiR.string.notif_chan_worker_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = applicationContext.getString(coreUiR.string.notif_chan_worker_desc)
            }
        )
    }
}