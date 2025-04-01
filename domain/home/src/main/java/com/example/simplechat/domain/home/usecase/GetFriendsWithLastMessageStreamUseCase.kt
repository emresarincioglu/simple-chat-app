package com.example.simplechat.domain.home.usecase

import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import com.example.simplechat.data.home.repository.HomeRepository
import javax.inject.Inject

class GetFriendsWithLastMessageStreamUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
    private val authRepository: AuthenticationRepository
) {
    operator fun invoke() = homeRepository.getFriendsWithLastMessageStream(
        authRepository.localUserId!!, authRepository.remoteUserId!!
    )
}