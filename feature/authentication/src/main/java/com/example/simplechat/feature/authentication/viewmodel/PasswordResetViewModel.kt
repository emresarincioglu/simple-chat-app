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
class PasswordResetViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : ViewModel() {
    var email by mutableStateOf(savedStateHandle["email"] ?: "")

    private val _sendPasswordResetEmailResult = MutableSharedFlow<Result<Boolean>>()
    val sendPasswordResetEmailResult = _sendPasswordResetEmailResult.asSharedFlow()

    fun sendPasswordResetEmail() {
        // TODO: Send password reset email
        viewModelScope.launch {
            _sendPasswordResetEmailResult.emit(Result.Loading)
        }
    }
}