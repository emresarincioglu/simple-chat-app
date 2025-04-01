package com.example.simplechat.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.simplechat.core.database.entity.FriendEntity
import com.example.simplechat.core.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {

    @Query("SELECT * FROM friends WHERE uid = :friendId LIMIT 1")
    suspend fun getFriend(friendId: Int): FriendEntity

    @Query("SELECT * FROM friends WHERE uid = :friendId LIMIT 1")
    fun getFriendStream(friendId: Int): Flow<FriendEntity?>

    @Query("SELECT * FROM friends WHERE user_id = :userId")
    suspend fun getFriends(userId: Int): List<FriendEntity>

    @Query(
        "SELECT * FROM friends AS f " +
                "LEFT JOIN messages AS m ON m.friend_id = f.uid " +
                "WHERE f.user_id = :userId " +
                "GROUP BY f.uid " +
                "HAVING MAX(m.time) OR m.time IS NULL"
    )
    fun getFriendsWithLastMessageStream(userId: Int): Flow<Map<FriendEntity, MessageEntity?>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(friend: FriendEntity): Long

    @Transaction
    suspend fun addAll(userId: Int, friends: List<FriendEntity>) {
        for (friend in friends) {
            val alreadyExists = add(friend) == -1L
            if (alreadyExists) {
                update(userId, friend.remoteRecordId, friend.name, friend.avatar)
            }
        }
    }

    @Query("UPDATE friends SET name = :name, avatar_url = :avatar WHERE uid = :friendId")
    suspend fun update(friendId: Int, name: String, avatar: String?)

    @Query(
        "UPDATE friends SET name = :name, avatar_url = :avatar " +
                "WHERE user_id = :userId AND remote_record_id = :recordId"
    )
    suspend fun update(userId: Int, recordId: String, name: String, avatar: String?)

    @Delete
    suspend fun delete(friend: FriendEntity)

    @Query("DELETE FROM friends WHERE user_id = :userId AND remote_record_id = :recordId")
    suspend fun delete(userId: Int, recordId: String)
}