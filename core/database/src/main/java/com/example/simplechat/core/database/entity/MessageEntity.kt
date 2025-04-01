package com.example.simplechat.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = FriendEntity::class,
            parentColumns = ["uid"],
            childColumns = ["friend_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("friend_id"), Index("time", "is_from_user", unique = true)]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "friend_id") val friendId: Int,
    @ColumnInfo(name = "is_from_user") val isFromUser: Boolean,
    val text: String?,
    @ColumnInfo(name = "image_url") val image: String?,
    val time: Long
)