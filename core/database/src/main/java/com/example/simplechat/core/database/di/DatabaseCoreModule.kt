package com.example.simplechat.core.database.di

import android.content.Context
import androidx.room.Room
import com.example.simplechat.core.database.ChatDatabase
import com.example.simplechat.core.database.datasource.FriendLocalDataSource
import com.example.simplechat.core.database.datasource.MessageLocalDataSource
import com.example.simplechat.core.database.datasource.UserLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseCoreModule {

    @Provides
    @Singleton
    fun provideChatDatabase(@ApplicationContext context: Context): ChatDatabase {
        return Room.databaseBuilder(context, ChatDatabase::class.java, "chats").build()
    }

    @Provides
    @Singleton
    fun provideUserRemoteDataSource(database: ChatDatabase): UserLocalDataSource {
        return UserLocalDataSource(database)
    }

    @Provides
    @Singleton
    fun provideAuthenticationRemoteDataSource(database: ChatDatabase): FriendLocalDataSource {
        return FriendLocalDataSource(database)
    }

    @Provides
    @Singleton
    fun provideFriendRemoteDataSource(database: ChatDatabase): MessageLocalDataSource {
        return MessageLocalDataSource(database)
    }
}