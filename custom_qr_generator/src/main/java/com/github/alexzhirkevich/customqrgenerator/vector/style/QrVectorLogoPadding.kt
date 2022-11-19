package com.github.alexzhirkevich.customqrgenerator.vector.style

import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

sealed interface QrVectorLogoPadding {

    val value : Float

    @kotlinx.serialization.Serializable
    @SerialName("Empty")
    object Empty : QrVectorLogoPadding {
        override val value: Float get() = 0f
    }

    @kotlinx.serialization.Serializable
    @SerialName("Accurate")
    class Accurate(override val value: Float) : QrVectorLogoPadding

    @kotlinx.serialization.Serializable
    @SerialName("Natural")
    class Natural(override val value: Float) : QrVectorLogoPadding

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