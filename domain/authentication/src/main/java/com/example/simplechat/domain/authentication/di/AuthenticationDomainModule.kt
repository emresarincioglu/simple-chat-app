package com.example.simplechat.domain.authentication.di

import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import com.example.simplechat.data.authentication.repository.SyncRepository
import com.example.simplechat.domain.authentication.usecase.InitializeCacheUseCase
import com.example.simplechat.domain.authentication.usecase.LogInUseCase
import com.example.simplechat.domain.authentication.usecase.SendPasswordResetEmailUseCase
import com.example.simplechat.domain.authentication.usecase.SignUpUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthenticationDomainModule {

    @Provides
    @Singleton
    fun provideLogInUseCase(
        syncRepository: SyncRepository, authenticationRepository: AuthenticationRepository
    ): LogInUseCase {
        return LogInUseCase(syncRepository, authenticationRepository)
    }

    @Provides
    @Singleton
    fun provideSignUpUseCase(
        syncRepository: SyncRepository, authenticationRepository: AuthenticationRepository
    ): SignUpUseCase {
        return SignUpUseCase(syncRepository, authenticationRepository)
    }

    @Provides
    @Singleton
    fun provideSendPasswordResetEmailUseCase(authenticationRepository: AuthenticationRepository): SendPasswordResetEmailUseCase {
        return SendPasswordResetEmailUseCase(authenticationRepository)
    }

    @Provides
    @Singleton
    fun provideInitializeCacheUseCase(authenticationRepository: AuthenticationRepository): InitializeCacheUseCase {
        return InitializeCacheUseCase(authenticationRepository)
    }
}