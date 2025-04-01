package com.example.simplechat.domain.home.usecase

import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import com.example.simplechat.data.home.repository.HomeRepository
import javax.inject.Inject

class AddFriendUseCase @Inject constructor(
    private val homeRepository: HomeRepository, private val authRepository: AuthenticationRepository
) {
    suspend operator fun invoke(friendCode: String): Boolean {
        return homeRepository.addFriend(authRepository.remoteUserId!!, friendCode)
    }
}