package com.example.simplechat.domain.home.usecase

import android.net.Uri
import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import com.example.simplechat.data.home.repository.HomeRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val homeRepository: HomeRepository, private val authRepository: AuthenticationRepository
) {
    suspend operator fun invoke(friendId: Int, message: String): Boolean {
        return homeRepository.sendMessage(authRepository.remoteUserId!!, friendId, message)
    }

    suspend operator fun invoke(friendId: Int, image: Uri) {
        homeRepository.sendImageMessage(authRepository.remoteUserId!!, friendId, image)
    }
}