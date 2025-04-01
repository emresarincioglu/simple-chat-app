package com.example.simplechat.domain.profile.usecase

import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import com.example.simplechat.data.authentication.repository.SyncRepository
import com.example.simplechat.data.home.repository.HomeRepository
import com.example.simplechat.data.profile.repository.ProfileRepository
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
    private val syncRepository: SyncRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthenticationRepository
) {
    suspend operator fun invoke(): Boolean {
        val isSuccessful = profileRepository.deleteUser(
            authRepository.localUserId!!, authRepository.remoteUserId!!
        )

        if (isSuccessful) {
            authRepository.clearCache()
            homeRepository.clearCache()
            syncRepository.cancelPeriodicSync()
        }

        return isSuccessful
    }
}