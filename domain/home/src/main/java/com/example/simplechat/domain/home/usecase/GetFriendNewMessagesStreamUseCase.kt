package com.example.simplechat.domain.home.usecase

import com.example.simplechat.core.common.model.message.Message
import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import com.example.simplechat.data.home.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFriendNewMessagesStreamUseCase @Inject constructor(
    private val homeRepository: HomeRepository, private val authRepository: AuthenticationRepository
) {
    operator fun invoke(friendId: Int, onOverFlow: () -> Unit, maxSize: Int): Flow<List<Message>> {
        val userId = authRepository.remoteUserId!!
        return homeRepository.getFriendNewMessagesStream(userId, friendId, onOverFlow, maxSize)
    }
}