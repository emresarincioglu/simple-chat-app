package com.example.simplechat.domain.authentication.usecase

import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import javax.inject.Inject

class InitializeCacheUseCase @Inject constructor(private val authRepository: AuthenticationRepository) {
    suspend operator fun invoke() = authRepository.initialize()
}