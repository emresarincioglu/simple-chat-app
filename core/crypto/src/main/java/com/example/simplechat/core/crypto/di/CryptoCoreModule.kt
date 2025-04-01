package com.example.simplechat.core.crypto.di

import com.example.simplechat.core.crypto.CryptoManager
import com.example.simplechat.core.datastore.DataStoreSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CryptoCoreModule {
    @Provides
    @Singleton
    fun provideCryptoManager(dataStoreSource: DataStoreSource): CryptoManager {
        return CryptoManager(dataStoreSource)
    }
}