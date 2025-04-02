package com.example.simplechat.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplechat.domain.home.usecase.AddFriendUseCase
import com.example.simplechat.domain.home.usecase.GetFriendsWithLastMessageStreamUseCase
import com.example.simplechat.domain.home.usecase.GetUserFriendCodeUseCase
import com.example.simplechat.domain.home.usecase.IsLoggedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getFriendsWithLastMessageStreamUseCase: GetFriendsWithLastMessageStreamUseCase,
    private val addFriendUseCase: AddFriendUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase,
    private val getUserFriendCodeUseCase: GetUserFriendCodeUseCase
) : ViewModel() {

    companion object {
        private const val FLOW_TIMEOUT = 5000L
    }

    val isLoggedIn get() = isLoggedInUseCase()

    val friendsStream = getFriendsWithLastMessageStreamUseCase().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT), emptyList()
    )

    fun getUserFriendCode() = getUserFriendCodeUseCase()

    fun addFriend(friendCode: String) {
        viewModelScope.launch {
            addFriendUseCase(friendCode)
        }
    }
}