package com.example.simplechat.data.home.worker

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
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.simplechat.core.ui.R
import com.example.simplechat.core.ui.createTempFile
import com.example.simplechat.data.home.repository.HomeRepository
import com.example.simplechat.data.home.worker.WorkerConstraints.DATA_COMPRESSED_IMAGE_FILE_NAME
import com.example.simplechat.data.home.worker.WorkerConstraints.DATA_FRIEND_ENCRYPTED_IMAGE_FILE_NAME
import com.example.simplechat.data.home.worker.WorkerConstraints.DATA_FRIEND_PUBLIC_KEY
import com.example.simplechat.data.home.worker.WorkerConstraints.DATA_USER_ENCRYPTED_IMAGE_FILE_NAME
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class EncryptImageWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: HomeRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val NOTIFICATION_ID = 3
    }

    override suspend fun doWork(): Result {
        val cacheDir = applicationContext.cacheDir
        val publicKey = inputData.getString(DATA_FRIEND_PUBLIC_KEY)!!
        val compressedImageFileName = inputData.getString(DATA_COMPRESSED_IMAGE_FILE_NAME)!!

        val compressedImageFile = File(cacheDir, compressedImageFileName)
        if (!compressedImageFile.exists()) {
            return Result.failure()
        }

        val extension = ".encrypt${compressedImageFile.extension}"
        val userEncryptedImageFile = applicationContext.createTempFile(extension)
            ?: return Result.failure()
        val friendEncryptedImageFile = applicationContext.createTempFile(extension)
            ?: return Result.failure()

        return try {
            compressedImageFile.inputStream().use { inStream ->
                userEncryptedImageFile.outputStream().use { outStream ->
                    repository._encryptImage(inStream, outStream)
                }
            }
            compressedImageFile.inputStream().use { inStream ->
                friendEncryptedImageFile.outputStream().use { outStream ->
                    repository._encryptImage(inStream, outStream, publicKey)
                }
            }
            compressedImageFile.delete()

            Result.success(
                Data.Builder()
                    .putString(DATA_USER_ENCRYPTED_IMAGE_FILE_NAME, userEncryptedImageFile.name)
                    .putString(DATA_FRIEND_ENCRYPTED_IMAGE_FILE_NAME, friendEncryptedImageFile.name)
                    .build()
            )
        } catch (ex: Exception) {
            Result.failure()
        }
    }

    override suspend fun getForegroundInfo() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ForegroundInfo(
            NOTIFICATION_ID,
            createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    } else {
        ForegroundInfo(NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        return NotificationCompat.Builder(
            applicationContext, applicationContext.getString(R.string.notif_chan_worker_id)
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(applicationContext.getString(R.string.notif_encrypt_image_worker_content))
            .setOngoing(true)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        NotificationManagerCompat.from(applicationContext).createNotificationChannel(
            NotificationChannel(
                applicationContext.getString(R.string.notif_chan_worker_id),
                applicationContext.getString(R.string.notif_chan_worker_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = applicationContext.getString(R.string.notif_chan_worker_desc)
            }
        )
    }
}