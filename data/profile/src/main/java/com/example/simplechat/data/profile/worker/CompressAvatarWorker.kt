package com.example.simplechat.data.profile.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.simplechat.core.ui.createTempFile
import com.example.simplechat.data.profile.worker.WorkerConstraints.DATA_COMPRESSED_IMAGE_FILE_NAME
import com.example.simplechat.data.profile.worker.WorkerConstraints.DATA_IMAGE_URI
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt
import com.example.simplechat.core.ui.R as coreUiR


class CompressAvatarWorker(
    context: Context, workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val MAX_IMAGE_RESOLUTION = 1080
        private const val MAX_IMAGE_SIZE = 200 * 1024L
    }

    override suspend fun doWork(): Result {
        val imageUri = inputData.getString(DATA_IMAGE_URI)!!.toUri()

        return try {
            val mimeType = applicationContext.contentResolver.getType(imageUri)
            val (compressFormat, fileExtension) = when (mimeType) {
                "image/png" -> Bitmap.CompressFormat.PNG to ".png"
                "image/jpeg" -> Bitmap.CompressFormat.JPEG to ".jpeg"
                "image/webp" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSLESS to ".webp"
                } else Bitmap.CompressFormat.WEBP to ".webp"

                else -> Bitmap.CompressFormat.JPEG to ".jpeg"
            }

            var image = getImage(imageUri) ?: return Result.failure()
            image = scaleImage(image)
            image = rotateImage(imageUri, image)
            val compressedImageFileName = compressImage(image, compressFormat, fileExtension)
                ?: return Result.failure()

            Result.success(
                Data.Builder()
                    .putString(DATA_COMPRESSED_IMAGE_FILE_NAME, compressedImageFileName)
                    .build()
            )
        } catch (ex: Exception) {
            Result.failure()
        }
    }

    private fun getImage(uri: Uri): Bitmap? {
        return applicationContext.contentResolver.openInputStream(uri).use { inStream ->
            BitmapFactory.decodeStream(inStream)
        }
    }

    private fun scaleImage(image: Bitmap): Bitmap {
        val scaledImage = image.scale(
            image.width.coerceAtMost(MAX_IMAGE_RESOLUTION),
            image.height.coerceAtMost(MAX_IMAGE_RESOLUTION)
        )

        image.recycle()
        return scaledImage
    }

    private fun rotateImage(uri: Uri, image: Bitmap): Bitmap {
        var orientation = 0
        val columns = arrayOf(MediaStore.Images.Media.ORIENTATION)

        applicationContext.contentResolver.query(uri, columns, null, null, null).use { cursor ->
            if (cursor?.moveToFirst() == true) {
                orientation = cursor.getInt(0)
            }
        }
        if (orientation == 0) return image

        val matrix = Matrix()
        matrix.postRotate(orientation.toFloat())
        val rotatedImage = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)

        image.recycle()
        return rotatedImage
    }

    private fun compressImage(
        image: Bitmap, format: Bitmap.CompressFormat, fileExtension: String
    ): String? {
        val compressedImageFile = applicationContext.createTempFile(fileExtension) ?: return null

        var quality = 80
        var imageBytes: ByteArray
        do {
            ByteArrayOutputStream().use { outStream ->
                image.compress(format, quality, outStream)
                imageBytes = outStream.toByteArray()
            }
            quality = (quality * 0.9).roundToInt()
        } while (imageBytes.size > MAX_IMAGE_SIZE && quality > 5 && format != Bitmap.CompressFormat.PNG)

        compressedImageFile.outputStream().use { outStream ->
            outStream.write(imageBytes, 0, imageBytes.size)
        }

        return compressedImageFile.name
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