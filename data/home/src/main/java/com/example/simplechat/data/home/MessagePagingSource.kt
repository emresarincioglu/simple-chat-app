package com.example.simplechat.data.home

import androidx.core.net.toUri
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.simplechat.core.common.model.message.ImageMessage
import com.example.simplechat.core.common.model.message.Message
import com.example.simplechat.core.common.model.message.TextMessage
import com.example.simplechat.core.database.datasource.MessageLocalDataSource

internal class MessagePagingSource(
    private val friendId: Int, private val messageSource: MessageLocalDataSource
) : PagingSource<Int, Message>() {

    companion object {
        private const val STARTING_PAGE = 1
    }

    private var oldestMessageTime = Long.MAX_VALUE

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Message> {
        val pageNumber = params.key ?: STARTING_PAGE
        return try {
            val messages = messageSource.getMessagesBefore(
                friendId, oldestMessageTime, params.loadSize
            )

            LoadResult.Page(
                data = messages.map { message ->
                    if (message.image == null) {
                        TextMessage(message.text!!, message.isFromUser, message.time)
                    } else {
                        ImageMessage(message.image!!.toUri(), message.isFromUser, message.time)
                    }
                },
                prevKey = if (pageNumber == STARTING_PAGE) null else pageNumber.minus(1),
                nextKey = if (messages.isEmpty()) null else {
                    oldestMessageTime = messages[messages.lastIndex].time
                    pageNumber.plus(1)
                }
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }


    override fun getRefreshKey(state: PagingState<Int, Message>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}