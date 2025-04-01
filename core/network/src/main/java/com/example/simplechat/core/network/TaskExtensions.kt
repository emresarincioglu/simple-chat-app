package com.example.simplechat.core.network

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun <T> Task<T>.awaitSuccessResult(): Boolean {
    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener {
            if (exception == null) {
                continuation.resume(isSuccessful)
            } else {
                continuation.resumeWithException(exception!!)
            }
        }
    }
}