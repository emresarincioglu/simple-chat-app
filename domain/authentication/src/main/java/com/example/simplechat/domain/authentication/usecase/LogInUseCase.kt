package com.example.simplechat.domain.authentication.usecase

import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import com.example.simplechat.data.authentication.repository.SyncRepository
import javax.inject.Inject

class LogInUseCase @Inject constructor(
    private val syncRepository: SyncRepository, private val authRepository: AuthenticationRepository
) {
    suspend operator fun invoke(email: String, password: String): Boolean {
        val isSuccessful = authRepository.logIn(email, password)

        if (isSuccessful) {
            syncRepository.enqueuePeriodicSync(
                authRepository.localUserId!!, authRepository.remoteUserId!!
            )
        }

        return isSuccessful
    }
}