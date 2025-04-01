package com.example.simplechat.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.simplechat.core.database.dao.FriendDao
import com.example.simplechat.core.database.dao.MessageDao
import com.example.simplechat.core.database.dao.UserDao
import com.example.simplechat.core.database.entity.FriendEntity
import com.example.simplechat.core.database.entity.MessageEntity
import com.example.simplechat.core.database.entity.UserEntity

@Database(
    entities = [UserEntity::class, MessageEntity::class, FriendEntity::class],
    exportSchema = false,
    version = 1
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun friendDao(): FriendDao
    abstract fun messageDao(): MessageDao
}