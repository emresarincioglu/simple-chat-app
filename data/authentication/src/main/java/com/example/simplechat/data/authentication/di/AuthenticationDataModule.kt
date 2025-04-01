package com.example.simplechat.data.authentication.di

import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import com.example.simplechat.data.authentication.repository.DefaultAuthenticationRepository
import com.example.simplechat.data.authentication.repository.DefaultSyncRepository
import com.example.simplechat.data.authentication.repository.SyncRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthenticationDataModule {
    @Binds
    @Singleton
    abstract fun bindAuthenticationRepository(repository: DefaultAuthenticationRepository): AuthenticationRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(repository: DefaultSyncRepository): SyncRepository
}