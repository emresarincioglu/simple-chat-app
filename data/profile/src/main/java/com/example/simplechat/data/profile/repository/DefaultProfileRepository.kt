package com.example.simplechat.data.profile.repository

import android.content.Context
import android.net.Uri
import androidx.core.graphics.scale
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.simplechat.core.common.di.IoDispatcher
import com.example.simplechat.core.common.model.User
import com.example.simplechat.core.crypto.CryptoManager
import com.example.simplechat.core.database.datasource.UserLocalDataSource
import com.example.simplechat.core.network.datasource.AuthenticationRemoteDataSource
import com.example.simplechat.core.network.datasource.UserRemoteDataSource
import com.example.simplechat.core.ui.getBitmap
import com.example.simplechat.data.profile.worker.CompressAvatarWorker
import com.example.simplechat.data.profile.worker.UploadAvatarWorker
import com.example.simplechat.data.profile.worker.WorkerConstraints.DATA_IMAGE_URI
import com.example.simplechat.data.profile.worker.WorkerConstraints.DATA_USER_ID
import com.example.simplechat.data.profile.worker.WorkerConstraints.WORK_NAME_SET_AVATAR
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class DefaultProfileRepository @Inject constructor(
    private val cryptoManager: CryptoManager,
    private val localUser: UserLocalDataSource,
    private val remoteUser: UserRemoteDataSource,
    private val remoteAuth: AuthenticationRemoteDataSource,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ProfileRepository {

    companion object {
        private const val CLASSIFICATION_IMAGE_SIZE = 180
    }

    override fun getUserStream(localUserId: Int, remoteUserId: String): Flow<User?> = channelFlow {

        launch(ioDispatcher) {
            remoteUser.getUserStream(remoteUserId).collect { user ->
                if (user.isActive) {
                    localUser.updateUser(localUserId, user.name, user.email, user.avatarUrl)
                }
            }
        }

        localUser.getUserStream(localUserId).flowOn(ioDispatcher).map { user ->
            user?.let { User(name = it.name, email = it.email, avatar = it.avatar) }
        }.collect(::send)
    }

    override suspend fun setUserName(remoteUserId: String, name: String): Boolean =
        withContext(ioDispatcher) {
            remoteUser.setUserName(remoteUserId, name)
        }

    override suspend fun setPassword(remoteUserId: String, password: String): Boolean =
        withContext(ioDispatcher) {
            val (iv, privateKey) = cryptoManager.getEncryptedPrivateKey(secretKey = password)
            remoteUser.setUserPassword(
                userId = remoteUserId,
                password = password,
                privateKey = privateKey,
                iv = iv
            )
        }

    override suspend fun setAvatar(remoteUserId: String, avatar: Uri?) {
        if (avatar == null) {
            _uploadAvatar(remoteUserId, null, "")
            return
        }

        WorkManager.getInstance(context)
            .beginUniqueWork(
                WORK_NAME_SET_AVATAR,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<CompressAvatarWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        WorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS
                    )
                    .setInputData(
                        Data.Builder()
                            .putString(DATA_IMAGE_URI, avatar.toString())
                            .build()
                    )
                    .build()
            )
            .then(
                OneTimeWorkRequestBuilder<UploadAvatarWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS
                    )
                    .setInputData(
                        Data.Builder()
                            .putString(DATA_USER_ID, remoteUserId)
                            .build()
                    )
                    .build()
            )
            .enqueue()
    }

    override suspend fun logOut() = withContext(ioDispatcher) {
        remoteAuth.logOut()
        cryptoManager.deleteData()
    }

    override suspend fun deleteUser(localUserId: Int, remoteUserId: String): Boolean {
        val isSuccessful = remoteUser.deleteUser(remoteUserId)

        if (isSuccessful) {
            localUser.deleteUser(localUserId)
            cryptoManager.deleteData()
        }

        return isSuccessful
    }

    override fun isViolenceImage(uri: Uri): Boolean {
        val bitmap = context.getBitmap(uri)
            ?.scale(CLASSIFICATION_IMAGE_SIZE, CLASSIFICATION_IMAGE_SIZE)
            ?: throw FileNotFoundException("Image not found")

        val inputBuffer = ByteBuffer.allocateDirect(
            CLASSIFICATION_IMAGE_SIZE * CLASSIFICATION_IMAGE_SIZE * Float.SIZE_BYTES * 3
        )
        inputBuffer.order(ByteOrder.nativeOrder())
        inputBuffer.rewind()

        val pixels = IntArray(CLASSIFICATION_IMAGE_SIZE * CLASSIFICATION_IMAGE_SIZE)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (pixel in pixels) {
            val red = ((pixel shr 16) and 0xFF).toFloat()
            val green = ((pixel shr 8) and 0xFF).toFloat()
            val blue = (pixel and 0xFF).toFloat()
            inputBuffer.putFloat(red)
            inputBuffer.putFloat(green)
            inputBuffer.putFloat(blue)
        }

        val output = arrayOf(FloatArray(2))
        val interpreter = Interpreter(FileUtil.loadMappedFile(context, "model.tflite"))

        interpreter.run(inputBuffer, output)
        interpreter.close()

        return output[0][1] > output[0][0]
    }

    /**
     * Should not be used in ViewModels
     */
    override suspend fun _uploadAvatar(
        userId: String, stream: InputStream?, fileExtension: String
    ) = withContext(ioDispatcher) {
        remoteUser.setUserAvatar(userId, stream, fileExtension)
    }
}