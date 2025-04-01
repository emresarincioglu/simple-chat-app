package com.example.simplechat.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.simplechat.core.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query(
        "SELECT * FROM messages " +
                "WHERE friend_id = :friendId AND time > :time " +
                "ORDER BY time DESC"
    )
    fun getMessagesAfterStream(friendId: Int, time: Long): Flow<List<MessageEntity>>

    @Query(
        "SELECT * FROM messages " +
                "WHERE friend_id = :friendId AND time < :time " +
                "ORDER BY time DESC " +
                "LIMIT :count"
    )
    suspend fun getMessagesBefore(friendId: Int, time: Long, count: Int): List<MessageEntity>

    @Query("SELECT time FROM messages WHERE friend_id = :friendId ORDER BY time DESC LIMIT 1")
    suspend fun getLastMessageTime(friendId: Int): Long?

    @Insert
    suspend fun add(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAll(message: List<MessageEntity>)

    @Update
    suspend fun update(message: MessageEntity)

    @Delete
    suspend fun delete(message: MessageEntity)
}