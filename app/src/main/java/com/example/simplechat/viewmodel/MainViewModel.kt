package com.example.simplechat.viewmodel

import androidx.lifecycle.ViewModel
import com.example.simplechat.domain.home.usecase.IsLoggedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val isLoggedInUseCase: IsLoggedInUseCase
) : ViewModel() {

    val isLoggedIn get() = isLoggedInUseCase()
}