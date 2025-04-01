package com.example.simplechat.core.crypto

import android.util.Base64

fun ByteArray.encodeToBase64(): String = Base64.encodeToString(this, Base64.DEFAULT)

fun String.decodeBase64(): ByteArray = Base64.decode(this, Base64.DEFAULT)