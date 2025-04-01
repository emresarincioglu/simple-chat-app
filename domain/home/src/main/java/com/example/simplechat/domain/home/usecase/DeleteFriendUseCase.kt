package com.example.simplechat.domain.home.usecase

import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import com.example.simplechat.data.home.repository.HomeRepository
import javax.inject.Inject

class DeleteFriendUseCase @Inject constructor(
    private val homeRepository: HomeRepository, private val authRepository: AuthenticationRepository
) {
    suspend operator fun invoke(friendId: Int): Boolean {
        return homeRepository.deleteFriend(authRepository.remoteUserId!!, friendId)
    }
}