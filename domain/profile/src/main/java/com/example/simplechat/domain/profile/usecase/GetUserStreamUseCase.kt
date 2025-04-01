package com.example.simplechat.domain.profile.usecase

import com.example.simplechat.core.common.model.User
import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import com.example.simplechat.data.profile.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserStreamUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthenticationRepository
) {
    operator fun invoke(): Flow<User?> {
        return profileRepository.getUserStream(authRepository.localUserId!!, authRepository.remoteUserId!!)
    }
}