package com.example.simplechat.core.network.model

data class NetworkUser(
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val isActive: Boolean
)
