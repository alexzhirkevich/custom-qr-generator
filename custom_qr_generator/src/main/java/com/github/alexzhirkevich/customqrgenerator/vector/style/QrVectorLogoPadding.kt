package com.github.alexzhirkevich.customqrgenerator.vector.style

import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import com.github.alexzhirkevich.customqrgenerator.style.QrLogoPadding.Accurate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Type of padding applied to the logo.
 * Padding applied even if logo drawable is not specified.
 *
 * Prefer empty padding if your qr code encodes large amount of data
 * to avoid performance issues.
 * */
sealed interface QrVectorLogoPadding {

    val value : Float


    /**
     * Logo will be drawn on top of QR code without any padding.
     * QR code pixels might be visible through transparent logo.
     *
     * Prefer empty padding if your qr code encodes large amount of data
     * to avoid performance issues.
     * */
    @kotlinx.serialization.Serializable
    @SerialName("Empty")
    object Empty : QrVectorLogoPadding {
        override val value: Float get() = 0f
    }


    /**
     * Padding will be applied directly according to the shape of logo.
     * Some QR code pixels can be cut.
     *
     * WARNING: this padding can cause performance issues for qr codes with
     * large amount out data
     * */
    @kotlinx.serialization.Serializable
    @SerialName("Accurate")
    data class Accurate(override val value: Float) : QrVectorLogoPadding


    /**
     * Works like [Accurate] but all clipped pixels will be removed.
     *
     * This padding can also cause a little performance issues wen applied
     * to large-data qr codes, but not as much as [Accurate].
     * */
    @kotlinx.serialization.Serializable
    @SerialName("Natural")
    data class Natural(override val value: Float) : QrVectorLogoPadding

    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        @Suppress("unchecked_cast")
        override val defaultSerializersModule: SerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModule {
                polymorphicDefaultSerializer(QrVectorLogoPadding::class){
                    Empty.serializer() as SerializationStrategy<QrVectorLogoPadding>
                }
                polymorphicDefaultDeserializer(QrVectorLogoPadding::class) {
                    Empty.serializer()
                }
                polymorphic(QrVectorLogoPadding::class){
                    subclass(Accurate::class)
                    subclass(Natural::class)
                    subclass(Empty::class)
                }
            }
        }
    }
}