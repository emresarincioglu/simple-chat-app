package com.example.simplechat.feature.profile.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplechat.core.common.Result
import com.example.simplechat.core.common.model.User
import com.example.simplechat.core.common.safeCall
import com.example.simplechat.domain.profile.usecase.DeleteUserUseCase
import com.example.simplechat.domain.profile.usecase.GetUserStreamUseCase
import com.example.simplechat.domain.profile.usecase.IsViolenceImageUseCase
import com.example.simplechat.domain.profile.usecase.LogOutUseCase
import com.example.simplechat.domain.profile.usecase.SetUserAvatarUseCase
import com.example.simplechat.domain.profile.usecase.SetUserNameUseCase
import com.example.simplechat.domain.profile.usecase.SetUserPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getUserStreamUseCase: GetUserStreamUseCase,
    private val logOutUseCase: LogOutUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val setUserNameUseCase: SetUserNameUseCase,
    private val setUserAvatarUseCase: SetUserAvatarUseCase,
    private val setUserPasswordUseCase: SetUserPasswordUseCase,
    private val isViolenceImageUseCase: IsViolenceImageUseCase
) : ViewModel() {

    companion object {
        private const val FLOW_TIMEOUT = 5000L
        private const val ACTION_TIMEOUT = 12_000L
    }

    private val _logOutResultStream = MutableSharedFlow<Result<Boolean>>()
    val logOutResultStream = _logOutResultStream.asSharedFlow()

    private val _changePasswordResultStream = MutableSharedFlow<Result<Boolean>>()
    val changePasswordResultStream = _changePasswordResultStream.asSharedFlow()

    private val _deleteUserResultStream = MutableSharedFlow<Result<Boolean>>()
    val deleteUserResultStream = _deleteUserResultStream.asSharedFlow()

    val userStream = getUserStreamUseCase().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT), User()
    )

    fun changeUserName(name: String) {
        viewModelScope.launch {
            setUserNameUseCase(name)
        }
    }

    fun changeUserPassword(password: String) {
        viewModelScope.launch {
            _changePasswordResultStream.emit(Result.Loading)
            _changePasswordResultStream.emit(safeCall(ACTION_TIMEOUT) {
                setUserPasswordUseCase(password)
            })
        }
    }

    fun changeUserAvatar(avatar: Uri?) {
        viewModelScope.launch {
            setUserAvatarUseCase(avatar)
        }
    }

    fun logOut() {
        viewModelScope.launch {
            _logOutResultStream.emit(Result.Loading)
            _logOutResultStream.emit(safeCall(ACTION_TIMEOUT) {
                logOutUseCase()
                true
            })
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            _deleteUserResultStream.emit(Result.Loading)
            _deleteUserResultStream.emit(safeCall(ACTION_TIMEOUT) { deleteUserUseCase() })
        }
    }

    fun isImageSafe(image: Uri) = !isViolenceImageUseCase(image)
}