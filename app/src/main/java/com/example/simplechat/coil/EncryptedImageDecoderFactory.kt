package com.example.simplechat.coil

import coil3.ImageLoader
import coil3.decode.Decoder
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.example.simplechat.core.crypto.CryptoManager
import javax.inject.Inject

class EncryptedImageDecoderFactory @Inject constructor(private val cryptoManager: CryptoManager) :
    Decoder.Factory {

    override fun create(
        result: SourceFetchResult, options: Options, imageLoader: ImageLoader
    ): Decoder? {
        val type = result.mimeType.orEmpty().substringAfter("image/encrypt", "")
        return if (type.isNotEmpty()) {
            EncryptedImageDecoder(result.source, cryptoManager)
        } else {
            null
        }
    }
}