package com.example.simplechat.domain.profile.usecase

import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import com.example.simplechat.data.profile.repository.ProfileRepository
import javax.inject.Inject

class SetUserNameUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthenticationRepository
) {
    suspend operator fun invoke(name: String): Boolean {
        return profileRepository.setUserName(authRepository.remoteUserId!!, name)
    }
}