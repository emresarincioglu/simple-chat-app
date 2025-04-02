package com.example.simplechat.feature.authentication.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplechat.core.common.Result
import com.example.simplechat.core.common.safeCall
import com.example.simplechat.domain.authentication.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    companion object {
        private const val ACTION_TIMEOUT = 10_000L
    }

    var name by mutableStateOf("")
    var email by mutableStateOf(savedStateHandle["email"] ?: "")
    var password by mutableStateOf(savedStateHandle["password"] ?: "")

    private val _signupResult = MutableSharedFlow<Result<Boolean>>()
    val signupResult = _signupResult.asSharedFlow()

    fun signUp() {
        viewModelScope.launch {
            _signupResult.emit(Result.Loading)
            _signupResult.emit(
                safeCall(ACTION_TIMEOUT) { signUpUseCase(name, email, password) }
            )
        }
    }
}