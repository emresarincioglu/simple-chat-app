package com.example.simplechat.core.database.datasource

import com.example.simplechat.core.database.ChatDatabase
import com.example.simplechat.core.database.entity.UserEntity
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class UserLocalDataSource @Inject constructor(private val database: ChatDatabase) {

    suspend fun getUser(email: String) = database.userDao().getByEmail(email)

    fun getUserStream(userId: Int) = database.userDao().getUserStream(userId).distinctUntilChanged()

    suspend fun addUser(name: String, email: String, avatar: String? = null): Long {
        return database.userDao().add(UserEntity(name = name, email = email, avatar = avatar))
    }

    suspend fun updateUser(userId: Int, name: String, email: String, avatar: String? = null) {
        database.userDao().update(
            UserEntity(uid = userId, name = name, email = email, avatar = avatar)
        )
    }

    suspend fun deleteUser(userId: Int) = database.userDao().delete(
        UserEntity(userId, "", "", null)
    )
}