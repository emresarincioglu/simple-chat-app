package com.example.simplechat.core.common.model.message

import android.net.Uri
import androidx.annotation.IntRange

data class ImageMessage(
    val imageUrl: Uri,
    override val isFromUser: Boolean,
    @IntRange(from = 0) override val time: Long
) : Message
