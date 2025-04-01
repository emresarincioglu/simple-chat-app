package com.example.simplechat.data.home.di

import com.example.simplechat.data.home.repository.DefaultHomeRepository
import com.example.simplechat.data.home.repository.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeDataModule {
    @Binds
    @Singleton
    abstract fun bindHomeRepository(repository: DefaultHomeRepository): HomeRepository
}