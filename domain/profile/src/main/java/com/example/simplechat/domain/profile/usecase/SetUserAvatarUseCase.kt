package com.example.simplechat.domain.profile.usecase

import android.net.Uri
import com.example.simplechat.data.authentication.repository.AuthenticationRepository
import com.example.simplechat.data.profile.repository.ProfileRepository
import javax.inject.Inject

class SetUserAvatarUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthenticationRepository
) {
    suspend operator fun invoke(avatar: Uri?) {
        return profileRepository.setAvatar(authRepository.remoteUserId!!, avatar)
    }
}