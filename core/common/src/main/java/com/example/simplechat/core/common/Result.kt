package com.example.simplechat.core.common

import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeoutException

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

/**
 * @param timeout Action timeout as milliseconds
 */
suspend fun <T> safeCall(timeout: Long? = null, call: suspend () -> T) = try {
    if (timeout == null) {
        Result.Success(call())
    } else {
        withTimeoutOrNull(timeMillis = timeout) {
            Result.Success(call())
        } ?: Result.Error(TimeoutException("Action timed out."))
    }
} catch (ex: Throwable) {
    Result.Error(ex)
}