package com.example.simplechat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplechat.domain.authentication.usecase.InitializeCacheUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val initializeCacheUseCase: InitializeCacheUseCase
) : ViewModel() {

    private val _isAppInitializedStream = MutableStateFlow(false)
    val isAppInitializedStream = _isAppInitializedStream.asStateFlow()

    init {
        viewModelScope.launch {
            _isAppInitializedStream.emit(false)
            initializeCacheUseCase()
            _isAppInitializedStream.emit(true)
        }
    }
}