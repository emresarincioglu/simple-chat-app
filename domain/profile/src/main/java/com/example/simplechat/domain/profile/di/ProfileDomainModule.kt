package com.example.simplechat.domain.profile.di

import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import com.example.simplechat.data.authentication.repository.SyncRepository
import com.example.simplechat.data.home.repository.HomeRepository
import com.example.simplechat.data.profile.repository.ProfileRepository
import com.example.simplechat.domain.profile.usecase.DeleteUserUseCase
import com.example.simplechat.domain.profile.usecase.GetUserStreamUseCase
import com.example.simplechat.domain.profile.usecase.IsViolenceImageUseCase
import com.example.simplechat.domain.profile.usecase.LogOutUseCase
import com.example.simplechat.domain.profile.usecase.SetUserAvatarUseCase
import com.example.simplechat.domain.profile.usecase.SetUserNameUseCase
import com.example.simplechat.domain.profile.usecase.SetUserPasswordUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProfileDomainModule {

    @Provides
    @Singleton
    fun provideLogOutUseCase(
        homeRepository: HomeRepository,
        syncRepository: SyncRepository,
        profileRepository: ProfileRepository,
        authenticationRepository: AuthenticationRepository
    ): LogOutUseCase {
        return LogOutUseCase(
            homeRepository, syncRepository, profileRepository, authenticationRepository
        )
    }

    @Provides
    @Singleton
    fun provideDeleteUserUseCase(
        homeRepository: HomeRepository,
        syncRepository: SyncRepository,
        profileRepository: ProfileRepository,
        authenticationRepository: AuthenticationRepository
    ): DeleteUserUseCase {
        return DeleteUserUseCase(
            homeRepository, syncRepository, profileRepository, authenticationRepository
        )
    }

    @Provides
    @Singleton
    fun provideGetUserStreamUseCase(
        profileRepository: ProfileRepository,
        authenticationRepository: AuthenticationRepository
    ): GetUserStreamUseCase {
        return GetUserStreamUseCase(profileRepository, authenticationRepository)
    }

    @Provides
    @Singleton
    fun provideSetUserNameUseCase(
        profileRepository: ProfileRepository,
        authenticationRepository: AuthenticationRepository
    ): SetUserNameUseCase {
        return SetUserNameUseCase(profileRepository, authenticationRepository)
    }

    @Provides
    @Singleton
    fun provideSetUserPasswordUseCase(
        profileRepository: ProfileRepository,
        authenticationRepository: AuthenticationRepository
    ): SetUserPasswordUseCase {
        return SetUserPasswordUseCase(profileRepository, authenticationRepository)
    }

    @Provides
    @Singleton
    fun provideSetUserAvatarUseCase(
        profileRepository: ProfileRepository,
        authenticationRepository: AuthenticationRepository
    ): SetUserAvatarUseCase {
        return SetUserAvatarUseCase(profileRepository, authenticationRepository)
    }

    @Provides
    @Singleton
    fun provideIsViolenceImageUseCase(profileRepository: ProfileRepository): IsViolenceImageUseCase {
        return IsViolenceImageUseCase(profileRepository)
    }
}