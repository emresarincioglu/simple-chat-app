package com.example.simplechat.core.network.datasource

import com.example.simplechat.core.network.NetworkConstraints.COLLECTION_FRIENDS
import com.example.simplechat.core.network.NetworkConstraints.COLLECTION_USERS
import com.example.simplechat.core.network.NetworkConstraints.FIELD_AVATAR_PATH
import com.example.simplechat.core.network.NetworkConstraints.FIELD_FRIEND_REF
import com.example.simplechat.core.network.NetworkConstraints.FIELD_IS_ACTIVE
import com.example.simplechat.core.network.NetworkConstraints.FIELD_PUBLIC_KEY
import com.example.simplechat.core.network.NetworkConstraints.FIELD_USER_NAME
import com.example.simplechat.core.network.awaitSuccessResult
import com.example.simplechat.core.network.model.NetworkFriend
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FriendRemoteDataSource @Inject constructor(
    private val db: FirebaseFirestore, private val storage: FirebaseStorage
) {

    suspend fun getFriends(userId: String) = db
        .collection("$COLLECTION_USERS/$userId/$COLLECTION_FRIENDS")
        .get().await()
        .map { record ->
            val isActive = record.getBoolean(FIELD_IS_ACTIVE) ?: true
            if (isActive) {
                val friend = record.getDocumentReference(FIELD_FRIEND_REF)!!.get().await()
                val avatarUrl = friend.getString(FIELD_AVATAR_PATH)?.let {
                    storage.getReferenceFromUrl(it).downloadUrl.await().toString()
                }

                NetworkFriend(
                    recordId = record.id,
                    name = friend.getString(FIELD_USER_NAME)!!,
                    avatarUrl = avatarUrl,
                    publicKey = friend.getString(FIELD_PUBLIC_KEY)!!,
                    isActive = friend.getBoolean(FIELD_IS_ACTIVE) ?: true
                )
            } else {
                NetworkFriend(recordId = record.id, isActive = false)
            }
        }

    suspend fun getFriendStream(userId: String, friendRecordId: String): Flow<NetworkFriend> {
        val userRef = db.document("$COLLECTION_USERS/$userId")
        val friendRecordRef = userRef.collection(COLLECTION_FRIENDS).document(friendRecordId)
        val friendRef = friendRecordRef.get().await().getDocumentReference(FIELD_FRIEND_REF)!!

        val isStillFriendStream = friendRecordRef.snapshots()
            .map { it.getBoolean(FIELD_IS_ACTIVE) ?: true }
            .distinctUntilChanged()

        return friendRef.snapshots().combine(isStillFriendStream) { friend, isFriend ->
            val avatarUrl = friend.getString(FIELD_AVATAR_PATH)?.let {
                storage.getReferenceFromUrl(it).downloadUrl.await().toString()
            }

            NetworkFriend(
                recordId = friendRecordId,
                name = friend.getString(FIELD_USER_NAME)!!,
                avatarUrl = avatarUrl,
                publicKey = friend.getString(FIELD_PUBLIC_KEY)!!,
                isActive = isFriend && friend.getBoolean(FIELD_IS_ACTIVE)!!
            )
        }.distinctUntilChanged()
    }

    fun getFriendsChangeStream(userId: String) = db
        .collection("$COLLECTION_USERS/$userId/$COLLECTION_FRIENDS")
        .snapshots()
        .map { snapshot ->
            snapshot.documentChanges.mapNotNull { change ->
                when (change.type) {
                    DocumentChange.Type.ADDED -> {
                        val record = change.document
                        val isActive = record.getBoolean(FIELD_IS_ACTIVE)!!

                        if (isActive) {
                            val friendRef = record.getDocumentReference(FIELD_FRIEND_REF)!!
                            val friend = friendRef.get().await()

                            if (friend.getBoolean(FIELD_IS_ACTIVE)!!) {
                                val avatarUrl = friend.getString(FIELD_AVATAR_PATH)?.let {
                                    storage.getReferenceFromUrl(it).downloadUrl.await().toString()
                                }

                                NetworkFriend(
                                    recordId = record.id,
                                    name = friend.getString(FIELD_USER_NAME)!!,
                                    avatarUrl = avatarUrl,
                                    publicKey = friend.getString(FIELD_PUBLIC_KEY)!!,
                                    isActive = true
                                )
                            } else {
                                record.reference.update(FIELD_IS_ACTIVE, false)
                                NetworkFriend(recordId = record.id, isActive = false)
                            }
                        } else {
                            NetworkFriend(recordId = record.id, isActive = false)
                        }
                    }

                    DocumentChange.Type.MODIFIED -> {
                        val record = change.document
                        val isActive = record.getBoolean(FIELD_IS_ACTIVE)!!

                        if (isActive) {
                            // Skip iteration
                            null
                        } else {
                            NetworkFriend(recordId = record.id, isActive = false)
                        }
                    }

                    else -> null
                }
            }
        }

    suspend fun getRecordPathOfUserFromFriend(userId: String, friendRecordId: String): String {
        val userRef = db.document("$COLLECTION_USERS/$userId")
        val friendRecord = userRef.collection(COLLECTION_FRIENDS).document(friendRecordId)

        val friendRef = friendRecord.get().await().getDocumentReference(FIELD_FRIEND_REF)!!
        return friendRef.collection(COLLECTION_FRIENDS)
            .whereEqualTo(FIELD_IS_ACTIVE, true)
            .whereEqualTo(FIELD_FRIEND_REF, userRef)
            .limit(1)
            .get().await()
            .documents[0]
            .reference
            .path
    }

    /**
     * @param userId Remote database user id
     * @param friendId Remote database user id of friend
     * @return Friend record unique id
     */
    suspend fun addFriend(userId: String, friendId: String): Boolean {
        if (isFriend(userId, friendId)) return true

        val userRef = db.document("$COLLECTION_USERS/$userId")
        val friendRef = db.document("$COLLECTION_USERS/$friendId")

        val friendRecordOfUser = userRef.collection(COLLECTION_FRIENDS).document()
        val friendRecordOfFriend = friendRef.collection(COLLECTION_FRIENDS).document()

        return db.runBatch { batch ->
            batch.set(
                friendRecordOfUser, mapOf(FIELD_FRIEND_REF to friendRef, FIELD_IS_ACTIVE to true)
            )
            batch.set(
                friendRecordOfFriend, mapOf(FIELD_FRIEND_REF to userRef, FIELD_IS_ACTIVE to true)
            )
        }.awaitSuccessResult()
    }

    suspend fun deleteFriend(userId: String, friendRecordId: String): Boolean {
        val userRef = db.document("$COLLECTION_USERS/$userId")
        val friendRecord = userRef.collection(COLLECTION_FRIENDS).document(friendRecordId)
        val friendRef = friendRecord.get().await().getDocumentReference(FIELD_FRIEND_REF)!!

        val friendRecordOfFriend = friendRef.collection(COLLECTION_FRIENDS)
            .whereEqualTo(FIELD_IS_ACTIVE, true)
            .whereEqualTo(FIELD_FRIEND_REF, userRef)
            .limit(1)
            .get().await()
            .documents[0]
            .reference

        return db.runBatch { batch ->
            batch.update(friendRecord, FIELD_IS_ACTIVE, false)
            batch.update(friendRecordOfFriend, FIELD_IS_ACTIVE, false)
        }.awaitSuccessResult()
    }

    private suspend fun isFriend(userId: String, friendId: String): Boolean {
        val friendRef = db.document("$COLLECTION_USERS/$friendId")
        val resultSize = db.collection("$COLLECTION_USERS/$userId/$COLLECTION_FRIENDS")
            .whereEqualTo(FIELD_IS_ACTIVE, true)
            .whereEqualTo(FIELD_FRIEND_REF, friendRef)
            .limit(1)
            .get().await()
            .documents
            .size

        return resultSize > 0
    }
}