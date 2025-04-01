package com.example.simplechat.domain.home.usecase

import com.example.simplechat.data.home.repository.HomeRepository
import javax.inject.Inject

class GetFriendPagedMessagesUseCase @Inject constructor(private val homeRepository: HomeRepository) {
    operator fun invoke(friendId: Int) = homeRepository.getFriendPagedMessages(friendId)
}