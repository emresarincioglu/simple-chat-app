package com.example.simplechat.core.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import java.io.File
import java.util.UUID

fun Context.getBitmap(uri: Uri) = try {
    contentResolver.openInputStream(uri).use { inStream ->
        BitmapFactory.decodeStream(inStream)
    }
} catch (ex: Exception) {
    null
}

fun Context.showToast(@StringRes resId: Int, duration: Int) {
    Toast.makeText(this, resId, duration).show()
}

fun Context.showToast(text: String, duration: Int) {
    Toast.makeText(this, text, duration).show()
}

fun Context.createTempFile(extension: String): File? {
    val file = File.createTempFile(UUID.randomUUID().toString(), extension, cacheDir)
    return if (file.exists()) file else null
}
