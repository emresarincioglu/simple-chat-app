package com.example.simplechat.core.network.model

data class NetworkFriend(
    val recordId: String,
    val isActive: Boolean,
    val name: String = "",
    val avatarUrl: String? = null,
    val publicKey: String = ""
)
