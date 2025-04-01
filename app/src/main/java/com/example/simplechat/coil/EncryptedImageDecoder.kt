package com.example.simplechat.coil

import android.graphics.BitmapFactory
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import com.example.simplechat.core.crypto.CryptoManager
import java.io.ByteArrayOutputStream

class EncryptedImageDecoder(
    private val source: ImageSource, private val cryptoManager: CryptoManager
) : Decoder {

    override suspend fun decode(): DecodeResult {
        val bitmap = source.source().inputStream().use { inStream ->
            val byteArray = ByteArrayOutputStream().use { outStream ->
                cryptoManager.decryptFile(inStream, outStream)
                outStream.toByteArray()
            }
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }

        return DecodeResult(image = bitmap.asImage(), isSampled = false)
    }
}