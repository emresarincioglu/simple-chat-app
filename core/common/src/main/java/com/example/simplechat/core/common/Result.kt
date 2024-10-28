package com.example.simplechat.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

suspend fun <T> safeCall(
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    call: suspend () -> Result<T>
): Result<T> {
    return try {
        withContext(dispatcher) {
            call()
        }
    } catch (e: Throwable) {
        Result.Error(e)
    }
}