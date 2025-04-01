package com.example.simplechat.core.network.model

data class CryptoKeysWithIv(
    val iv: String,
    val privateKey: String,
    val publicKey: String
)