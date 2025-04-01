package com.example.simplechat.core.database.datasource

import com.example.simplechat.core.database.ChatDatabase
import com.example.simplechat.core.database.entity.FriendEntity
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class FriendLocalDataSource @Inject constructor(private val database: ChatDatabase) {

    suspend fun getFriend(friendId: Int) = database.friendDao().getFriend(friendId)

    fun getFriendStream(friendId: Int) = database.friendDao()
        .getFriendStream(friendId)
        .distinctUntilChanged()

    suspend fun getFriends(userId: Int) = database.friendDao().getFriends(userId)

    fun getFriendsWithLastMessageStream(userId: Int) = database.friendDao()
        .getFriendsWithLastMessageStream(userId)
        .distinctUntilChanged()

    suspend fun addFriends(userId: Int, friends: List<FriendEntity>) {
        database.friendDao().addAll(userId, friends)
    }

    suspend fun updateFriend(friendId: Int, name: String, avatar: String?) {
        database.friendDao().update(friendId, name, avatar)
    }

    suspend fun deleteFriend(friendId: Int) = database.friendDao().delete(
        FriendEntity(friendId, -1, "", null, "", "")
    )

    suspend fun deleteFriend(userId: Int, recordId: String) {
        database.friendDao().delete(userId = userId, recordId = recordId)
    }
}