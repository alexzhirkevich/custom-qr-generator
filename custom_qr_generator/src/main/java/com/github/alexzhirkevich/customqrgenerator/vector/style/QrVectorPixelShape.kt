package com.github.alexzhirkevich.customqrgenerator.vector.style

import androidx.annotation.FloatRange
import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import com.github.alexzhirkevich.customqrgenerator.style.QrPixelShape
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Style of the qr-code pixels.
 * */
interface QrVectorPixelShape : QrVectorShapeModifier {

    @Serializable
    @SerialName("Default")
    object Default : QrVectorPixelShape, QrVectorShapeModifier by DefaultVectorShape

    @Serializable
    @SerialName("Circle")
    data class Circle(
        @FloatRange(from = 0.0, to = 1.0) val size: Float
    ) : QrVectorPixelShape, QrVectorShapeModifier by CircleVectorShape(size)

    @Serializable
    @SerialName("RoundCorners")
    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val radius : Float
    ) : QrVectorPixelShape, QrVectorShapeModifier by RoundCornersVectorShape(radius)  {

        override val isDependOnNeighbors: Boolean get() = true
    }

    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        @Suppress("unchecked_cast")
        override val defaultSerializersModule: SerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModule {
                polymorphicDefaultSerializer(QrVectorPixelShape::class){
                    Default.serializer() as SerializationStrategy<QrVectorPixelShape>
                }
                polymorphicDefaultDeserializer(QrVectorPixelShape::class) {
                    Default.serializer()
                }
                polymorphic(QrVectorPixelShape::class){
                    subclass(Default::class)
                    subclass(Circle::class)
                    subclass(RoundCorners::class)
                }
            }
        }
    }
}