package com.example.simplechat.domain.home.usecase

import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import javax.inject.Inject

class IsLoggedInUseCase @Inject constructor(private val authRepository: AuthenticationRepository) {
    operator fun invoke() = authRepository.isLoggedIn
}