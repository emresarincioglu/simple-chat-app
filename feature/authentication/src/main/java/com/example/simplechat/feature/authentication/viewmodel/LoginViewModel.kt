package com.example.simplechat.feature.authentication.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplechat.core.common.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    private val _loginResult = MutableSharedFlow<Result<Boolean>>()
    val loginResult = _loginResult.asSharedFlow()

    fun login() {
        // TODO: Login with email and password
        viewModelScope.launch {
            _loginResult.emit(Result.Success(true))
        }
    }
}