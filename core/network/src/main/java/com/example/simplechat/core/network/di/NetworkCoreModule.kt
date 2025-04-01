package com.example.simplechat.core.network.di

import com.example.simplechat.core.network.datasource.AuthenticationRemoteDataSource
import com.example.simplechat.core.network.datasource.FriendRemoteDataSource
import com.example.simplechat.core.network.datasource.MessageRemoteDataSource
import com.example.simplechat.core.network.datasource.UserRemoteDataSource
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkCoreModule {
    @Provides
    @Singleton
    fun provideUserRemoteDataSource(): UserRemoteDataSource {
        return UserRemoteDataSource(
            auth = Firebase.auth, db = Firebase.firestore, storage = Firebase.storage
        )
    }

    @Provides
    @Singleton
    fun provideAuthenticationRemoteDataSource(): AuthenticationRemoteDataSource {
        return AuthenticationRemoteDataSource(auth = Firebase.auth, db = Firebase.firestore)
    }

    @Provides
    @Singleton
    fun provideFriendRemoteDataSource(): FriendRemoteDataSource {
        return FriendRemoteDataSource(db = Firebase.firestore, storage = Firebase.storage)
    }

    @Provides
    @Singleton
    fun provideMessageRemoteDataSource(): MessageRemoteDataSource {
        return MessageRemoteDataSource(db = Firebase.firestore, storage = Firebase.storage)
    }
}