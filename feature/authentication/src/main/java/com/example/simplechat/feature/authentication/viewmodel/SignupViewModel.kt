package com.example.simplechat.feature.authentication.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplechat.core.common.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : ViewModel() {
    var name by mutableStateOf("")
    var email by mutableStateOf(savedStateHandle["email"] ?: "")
    var password by mutableStateOf(savedStateHandle["password"] ?: "")

    private val _signupResult = MutableSharedFlow<Result<Boolean>>()
    val signupResult = _signupResult.asSharedFlow()

    fun signUp() {
        // TODO: Sign up with name, email and password
        viewModelScope.launch {
            _signupResult.emit(Result.Loading)
        }
    }
}