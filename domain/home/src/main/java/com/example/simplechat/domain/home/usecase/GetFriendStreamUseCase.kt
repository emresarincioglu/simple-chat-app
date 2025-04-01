package com.example.simplechat.domain.home.usecase

import com.example.simplechat.core.common.model.Friend
import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import com.example.simplechat.data.home.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFriendStreamUseCase @Inject constructor(
    private val homeRepository: HomeRepository, private val authRepository: AuthenticationRepository
) {
    operator fun invoke(friendId: Int): Flow<Friend?> {
        return homeRepository.getFriendStream(authRepository.remoteUserId!!, friendId)
    }
}