package com.example.simplechat.core.network.datasource

import com.example.simplechat.core.common.model.message.ImageMessage
import com.example.simplechat.core.common.model.message.Message
import com.example.simplechat.core.common.model.message.TextMessage
import com.example.simplechat.core.network.NetworkConstraints.COLLECTION_FRIENDS
import com.example.simplechat.core.network.NetworkConstraints.COLLECTION_MESSAGES
import com.example.simplechat.core.network.NetworkConstraints.COLLECTION_USERS
import com.example.simplechat.core.network.NetworkConstraints.FIELD_LAST_MESSAGE_TIMESTAMP
import com.example.simplechat.core.network.NetworkConstraints.FIELD_MESSAGE_FROM_USER
import com.example.simplechat.core.network.NetworkConstraints.FIELD_MESSAGE_IMAGE_PATH
import com.example.simplechat.core.network.NetworkConstraints.FIELD_MESSAGE_TEXT
import com.example.simplechat.core.network.NetworkConstraints.FIELD_MESSAGE_TIMESTAMP
import com.example.simplechat.core.network.NetworkConstraints.FOLDER_ENCRYPTED_IMAGES
import com.example.simplechat.core.network.awaitSuccessResult
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class MessageRemoteDataSource @Inject constructor(
    private val db: FirebaseFirestore, private val storage: FirebaseStorage
) {

    suspend fun getLastMessageTime(userId: String, friendRecordId: String) = db
        .document("$COLLECTION_USERS/$userId/$COLLECTION_FRIENDS/$friendRecordId")
        .get().await()
        .getTimestamp(FIELD_LAST_MESSAGE_TIMESTAMP)?.toDate()?.time

    fun getLastMessageTimeStream(userId: String, friendRecordId: String) = db
        .document("$COLLECTION_USERS/$userId/$COLLECTION_FRIENDS/$friendRecordId")
        .snapshots()
        .map { it.getTimestamp(FIELD_LAST_MESSAGE_TIMESTAMP)?.toDate()?.time }
        .distinctUntilChanged()

    suspend fun getMessagesAfter(
        userId: String, friendRecordId: String, time: Long, count: Long
    ): List<Message> {
        return db.collection("$COLLECTION_USERS/$userId/$COLLECTION_FRIENDS/$friendRecordId/$COLLECTION_MESSAGES")
            .whereGreaterThan(FIELD_MESSAGE_TIMESTAMP, Timestamp(Date(time)))
            .orderBy(FIELD_MESSAGE_TIMESTAMP, Query.Direction.ASCENDING)
            .limit(count)
            .get().await()
            .documents
            .map { message ->
                val imageUrl = message.getString(FIELD_MESSAGE_IMAGE_PATH)?.let {
                    storage.getReferenceFromUrl(it).downloadUrl.await()
                }

                if (imageUrl == null) {
                    TextMessage(
                        text = message.getString(FIELD_MESSAGE_TEXT)!!,
                        time = message.getTimestamp(FIELD_MESSAGE_TIMESTAMP)!!.toDate().time,
                        isFromUser = message.getBoolean(FIELD_MESSAGE_FROM_USER)!!
                    )
                } else {
                    ImageMessage(
                        imageUrl = imageUrl,
                        time = message.getTimestamp(FIELD_MESSAGE_TIMESTAMP)!!.toDate().time,
                        isFromUser = message.getBoolean(FIELD_MESSAGE_FROM_USER)!!
                    )
                }
            }
    }

    suspend fun sendMessage(
        userId: String,
        friendRecordId: String,
        recordPathOfUser: String,
        userEncryptedMessage: String,
        friendEncryptedMessage: String
    ): Boolean {
        val userRef = db.document("$COLLECTION_USERS/$userId")
        val friendRecord = userRef.collection(COLLECTION_FRIENDS).document(friendRecordId)
        val friendFriendRecord = db.document(recordPathOfUser)

        val userMessageDoc = friendRecord.collection(COLLECTION_MESSAGES).document()
        val friendMessageDoc = friendFriendRecord.collection(COLLECTION_MESSAGES).document()

        val timestamp = FieldValue.serverTimestamp()
        return db.runBatch { batch ->
            batch.set(
                userMessageDoc,
                mapOf(
                    FIELD_MESSAGE_TEXT to userEncryptedMessage,
                    FIELD_MESSAGE_TIMESTAMP to timestamp,
                    FIELD_MESSAGE_FROM_USER to true
                )
            )

            batch.set(
                friendMessageDoc,
                mapOf(
                    FIELD_MESSAGE_TEXT to friendEncryptedMessage,
                    FIELD_MESSAGE_TIMESTAMP to timestamp,
                    FIELD_MESSAGE_FROM_USER to false
                )
            )

            batch.set(
                friendRecord,
                mapOf(FIELD_LAST_MESSAGE_TIMESTAMP to timestamp),
                SetOptions.merge()
            )
            batch.set(
                friendFriendRecord,
                mapOf(FIELD_LAST_MESSAGE_TIMESTAMP to timestamp),
                SetOptions.merge()
            )
        }.awaitSuccessResult()
    }

    suspend fun sendImageMessage(
        userId: String,
        friendRecordId: String,
        recordPathOfUser: String,
        userEncryptedImageStream: InputStream,
        friendEncryptedImageStream: InputStream,
        fileExtension: String
    ) = coroutineScope {
        val userRef = db.document("$COLLECTION_USERS/$userId")
        val friendRecord = userRef.collection(COLLECTION_FRIENDS).document(friendRecordId)
        val friendFriendRecord = db.document(recordPathOfUser)

        val userMessageDoc = friendRecord.collection(COLLECTION_MESSAGES).document()
        val friendMessageDoc = friendFriendRecord.collection(COLLECTION_MESSAGES).document()

        val (userImagePath, friendImagePath) = awaitAll(
            async {
                val fileName = "${UUID.randomUUID()}.$fileExtension"
                val photoPath = storage.reference.child(FOLDER_ENCRYPTED_IMAGES).child(fileName)
                val metadata = StorageMetadata.Builder()
                    .setContentType("image/$fileExtension")
                    .build()

                photoPath.putStream(userEncryptedImageStream, metadata).await()
                photoPath.toString()
            },
            async {
                val fileName = "${UUID.randomUUID()}.$fileExtension"
                val photoPath = storage.reference.child(FOLDER_ENCRYPTED_IMAGES).child(fileName)
                val metadata = StorageMetadata.Builder()
                    .setContentType("image/$fileExtension")
                    .build()

                photoPath.putStream(friendEncryptedImageStream, metadata).await()
                photoPath.toString()
            }
        )

        val timestamp = FieldValue.serverTimestamp()
        db.runBatch { batch ->
            batch.set(
                userMessageDoc,
                mapOf(
                    FIELD_MESSAGE_IMAGE_PATH to userImagePath,
                    FIELD_MESSAGE_TIMESTAMP to timestamp,
                    FIELD_MESSAGE_FROM_USER to true
                )
            )

            batch.set(
                friendMessageDoc,
                mapOf(
                    FIELD_MESSAGE_IMAGE_PATH to friendImagePath,
                    FIELD_MESSAGE_TIMESTAMP to timestamp,
                    FIELD_MESSAGE_FROM_USER to false
                )
            )

            batch.set(
                friendRecord,
                mapOf(FIELD_LAST_MESSAGE_TIMESTAMP to timestamp),
                SetOptions.merge()
            )
            batch.set(
                friendFriendRecord,
                mapOf(FIELD_LAST_MESSAGE_TIMESTAMP to timestamp),
                SetOptions.merge()
            )
        }.awaitSuccessResult()
    }
}