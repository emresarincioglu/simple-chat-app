package com.example.simplechat.feature.home.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.InvalidatingPagingSourceFactory
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.simplechat.core.common.model.Friend
import com.example.simplechat.domain.home.usecase.DeleteFriendUseCase
import com.example.simplechat.domain.home.usecase.GetFriendNewMessagesStreamUseCase
import com.example.simplechat.domain.home.usecase.GetFriendPagedMessagesUseCase
import com.example.simplechat.domain.home.usecase.GetFriendStreamUseCase
import com.example.simplechat.domain.home.usecase.SendMessageUseCase
import com.example.simplechat.domain.profile.usecase.IsViolenceImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getFriendStreamUseCase: GetFriendStreamUseCase,
    getFriendNewMessagesStreamUseCase: GetFriendNewMessagesStreamUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val deleteFriendUseCase: DeleteFriendUseCase,
    private val isViolenceImageUseCase: IsViolenceImageUseCase,
    private val getFriendPagedMessagesUseCase: GetFriendPagedMessagesUseCase
) : ViewModel() {

    companion object {
        private const val FLOW_TIMEOUT = 5000L
    }

    private val friendId: Int = savedStateHandle["friendId"]!!
    private val pagingSourceFactory = InvalidatingPagingSourceFactory {
        getFriendPagedMessagesUseCase(friendId)
    }

    val friendStream = getFriendStreamUseCase(friendId).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT), Friend()
    )

    val oldMessagesStream = Pager(
        config = PagingConfig(pageSize = 20), pagingSourceFactory = pagingSourceFactory
    ).flow.distinctUntilChanged().cachedIn(viewModelScope)

    val newMessagesStream = getFriendNewMessagesStreamUseCase(
        friendId = friendId, maxSize = 40, onOverFlow = pagingSourceFactory::invalidate
    )

    fun sendMessage(message: String) {
        viewModelScope.launch {
            sendMessageUseCase(friendId, message)
        }
    }

    fun sendImage(image: Uri) {
        viewModelScope.launch {
            sendMessageUseCase(friendId, image)
        }
    }

    fun deleteFriend() {
        viewModelScope.launch {
            deleteFriendUseCase(friendId)
        }
    }

    fun isImageSafe(image: Uri) = !isViolenceImageUseCase(image)
}