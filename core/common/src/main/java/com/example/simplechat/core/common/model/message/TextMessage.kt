package com.example.simplechat.core.common.model.message

import androidx.annotation.IntRange

data class TextMessage(
    val text: String,
    override val isFromUser: Boolean,
    @IntRange(from = 0) override val time: Long
) : Message
