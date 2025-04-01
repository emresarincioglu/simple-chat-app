package com.example.simplechat.data.home.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.simplechat.core.ui.createTempFile
import com.example.simplechat.data.home.worker.WorkerConstraints.DATA_COMPRESSED_IMAGE_FILE_NAME
import com.example.simplechat.data.home.worker.WorkerConstraints.DATA_IMAGE_URI
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt
import com.example.simplechat.core.ui.R as coreUiR

class CompressImageWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    companion object {
        private const val NOTIFICATION_ID = 3
        private const val MAX_IMAGE_SIZE = 100 * 1024L
    }

    override suspend fun doWork(): Result {
        val contentResolver = applicationContext.contentResolver
        val imageUri = inputData.getString(DATA_IMAGE_URI)!!.toUri()

        return try {
            val mimeType = contentResolver.getType(imageUri)
            val (compressFormat, fileExtension) = when (mimeType) {
                "image/png" -> Bitmap.CompressFormat.PNG to ".png"
                "image/jpeg" -> Bitmap.CompressFormat.JPEG to ".jpeg"
                "image/webp" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSLESS to ".webp"
                } else Bitmap.CompressFormat.WEBP to ".webp"

                else -> Bitmap.CompressFormat.JPEG to ".jpeg"
            }

            val image = contentResolver.openInputStream(imageUri).use { inStream ->
                BitmapFactory.decodeStream(inStream)
            }!!

            val compressedImageFile = applicationContext.createTempFile(fileExtension)
                ?: return Result.failure()

            var quality = 70
            var imageBytes: ByteArray
            do {
                ByteArrayOutputStream().use { outStream ->
                    image.compress(compressFormat, quality, outStream)
                    imageBytes = outStream.toByteArray()
                }
                quality = (quality * 0.9).roundToInt()
            } while (imageBytes.size > MAX_IMAGE_SIZE && quality > 5 && compressFormat != Bitmap.CompressFormat.PNG)

            compressedImageFile.outputStream().use { outStream ->
                outStream.write(imageBytes, 0, imageBytes.size)
            }

            Result.success(
                Data.Builder()
                    .putString(DATA_COMPRESSED_IMAGE_FILE_NAME, compressedImageFile.name)
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
            applicationContext, applicationContext.getString(coreUiR.string.notif_chan_worker_id)
        )
            .setSmallIcon(coreUiR.mipmap.ic_launcher)
            .setContentTitle(applicationContext.getString(coreUiR.string.app_name))
            .setContentText(applicationContext.getString(coreUiR.string.notif_compress_image_worker_content))
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