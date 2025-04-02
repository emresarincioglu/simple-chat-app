package com.example.simplechat.feature.authentication.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplechat.core.common.Result
import com.example.simplechat.core.common.safeCall
import com.example.simplechat.domain.authentication.usecase.LogInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val logInUseCase: LogInUseCase) : ViewModel() {

    companion object {
        private const val ACTION_TIMEOUT = 10_000L
    }

    var email by mutableStateOf("")
    var password by mutableStateOf("")

    private val _loginResult = MutableSharedFlow<Result<Boolean>>()
    val loginResult = _loginResult.asSharedFlow()

    private var loginJob: Job? = null

    fun logIn() {
        loginJob = viewModelScope.launch {
            _loginResult.emit(Result.Loading)
            _loginResult.emit(safeCall(ACTION_TIMEOUT) { logInUseCase(email, password) })
        }
    }

    fun cancelRunningActions() {
        loginJob?.cancel()
        if (loginJob?.isCompleted == false) {
            viewModelScope.launch {
                _loginResult.emit(Result.Success(false))
            }
        }
    }
}