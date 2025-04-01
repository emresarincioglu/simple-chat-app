package com.example.simplechat.domain.home.usecase

import android.graphics.Bitmap
import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import com.example.simplechat.data.home.repository.HomeRepository
import javax.inject.Inject

class GetUserFriendCodeUseCase @Inject constructor(
    private val homeRepository: HomeRepository, private val authRepository: AuthenticationRepository
) {
    operator fun invoke(): Pair<Bitmap, String> {
        val friendCode = authRepository.remoteUserId!!
        return homeRepository.getUserFriendCodeQr(friendCode) to friendCode
    }
}