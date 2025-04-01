package com.example.simplechat.domain.profile.usecase

import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import com.example.simplechat.data.profile.repository.ProfileRepository
import javax.inject.Inject

class SetUserPasswordUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthenticationRepository
) {
    suspend operator fun invoke(password: String): Boolean {
        return profileRepository.setPassword(authRepository.remoteUserId!!, password)
    }
}