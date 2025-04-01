package com.example.simplechat.data.profile.di

import com.example.simplechat.data.profile.repository.DefaultProfileRepository
import com.example.simplechat.data.profile.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileDataModule {
    @Binds
    @Singleton
    abstract fun bindProfileRepository(repository: DefaultProfileRepository): ProfileRepository
}