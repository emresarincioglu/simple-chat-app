package com.example.simplechat.domain.profile.usecase

import android.net.Uri
import com.example.simplechat.data.profile.repository.ProfileRepository
import javax.inject.Inject

class IsViolenceImageUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke(imageUri: Uri) = repository.isViolenceImage(imageUri)
}