package com.example.simplechat.di

import coil3.decode.Decoder
import com.example.simplechat.coil.EncryptedImageDecoderFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoilModule {
    @Binds
    @Singleton
    abstract fun bindCoilDecoderFactory(factory: EncryptedImageDecoderFactory): Decoder.Factory
}