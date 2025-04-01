package com.example.simplechat.core.common.model.message

interface Message {
    val time: Long
    val isFromUser: Boolean
}