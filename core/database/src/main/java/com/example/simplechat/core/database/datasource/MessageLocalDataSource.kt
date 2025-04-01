package com.example.simplechat.core.database.datasource

import com.example.simplechat.core.database.ChatDatabase
import com.example.simplechat.core.database.entity.MessageEntity
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class MessageLocalDataSource @Inject constructor(private val database: ChatDatabase) {

    fun getMessagesAfterStream(friendId: Int, time: Long) = database.messageDao()
        .getMessagesAfterStream(friendId, time)
        .distinctUntilChanged()

    suspend fun getMessagesBefore(friendId: Int, time: Long, count: Int): List<MessageEntity> {
        return database.messageDao().getMessagesBefore(friendId, time, count)
    }

    suspend fun getLastMessageTime(friendId: Int): Long? {
        return database.messageDao().getLastMessageTime(friendId)
    }

    suspend fun addMessages(messages: List<MessageEntity>) = database.messageDao().addAll(messages)
}