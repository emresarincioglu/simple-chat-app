package com.example.simplechat.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index("email", unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val name: String,
    val email: String,
    @ColumnInfo(name = "avatar_url") val avatar: String?
)
