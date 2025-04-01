package com.example.simplechat.domain.home.di

import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import com.example.simplechat.data.home.repository.HomeRepository
import com.example.simplechat.domain.home.usecase.AddFriendUseCase
import com.example.simplechat.domain.home.usecase.DeleteFriendUseCase
import com.example.simplechat.domain.home.usecase.GetFriendNewMessagesStreamUseCase
import com.example.simplechat.domain.home.usecase.GetFriendPagedMessagesUseCase
import com.example.simplechat.domain.home.usecase.GetFriendStreamUseCase
import com.example.simplechat.domain.home.usecase.GetFriendsWithLastMessageStreamUseCase
import com.example.simplechat.domain.home.usecase.GetUserFriendCodeUseCase
import com.example.simplechat.domain.home.usecase.IsLoggedInUseCase
import com.example.simplechat.domain.home.usecase.SendMessageUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HomeDomainModule {

    @Provides
    @Singleton
    fun provideIsLoggedInUseCase(authenticationRepository: AuthenticationRepository): IsLoggedInUseCase {
        return IsLoggedInUseCase(authenticationRepository)
    }

    @Provides
    @Singleton
    fun provideGetFriendsWithLastMessageStreamUseCase(
        homeRepository: HomeRepository, authenticationRepository: AuthenticationRepository
    ): GetFriendsWithLastMessageStreamUseCase {
        return GetFriendsWithLastMessageStreamUseCase(homeRepository, authenticationRepository)
    }

    @Provides
    @Singleton
    fun provideGetUserFriendCodeUseCase(
        homeRepository: HomeRepository, authenticationRepository: AuthenticationRepository
    ): GetUserFriendCodeUseCase {
        return GetUserFriendCodeUseCase(homeRepository, authenticationRepository)
    }

    @Provides
    @Singleton
    fun provideAddFriendUseCase(
        homeRepository: HomeRepository, authenticationRepository: AuthenticationRepository
    ): AddFriendUseCase {
        return AddFriendUseCase(homeRepository, authenticationRepository)
    }

    @Provides
    @Singleton
    fun provideGetFriendPagedMessagesUseCase(homeRepository: HomeRepository): GetFriendPagedMessagesUseCase {
        return GetFriendPagedMessagesUseCase(homeRepository)
    }

    @Provides
    @Singleton
    fun provideGetFriendStreamUseCase(
        homeRepository: HomeRepository, authenticationRepository: AuthenticationRepository
    ): GetFriendStreamUseCase {
        return GetFriendStreamUseCase(homeRepository, authenticationRepository)
    }

    @Provides
    @Singleton
    fun provideGetFriendNewMessagesStreamUseCase(
        homeRepository: HomeRepository, authenticationRepository: AuthenticationRepository
    ): GetFriendNewMessagesStreamUseCase {
        return GetFriendNewMessagesStreamUseCase(homeRepository, authenticationRepository)
    }

    @Provides
    @Singleton
    fun provideSendMessageUseCase(
        homeRepository: HomeRepository, authenticationRepository: AuthenticationRepository
    ): SendMessageUseCase {
        return SendMessageUseCase(homeRepository, authenticationRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteFriendUseCase(
        homeRepository: HomeRepository, authenticationRepository: AuthenticationRepository
    ): DeleteFriendUseCase {
        return DeleteFriendUseCase(homeRepository, authenticationRepository)
    }
}