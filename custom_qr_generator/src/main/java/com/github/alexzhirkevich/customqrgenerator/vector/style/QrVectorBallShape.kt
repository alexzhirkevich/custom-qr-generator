package com.github.alexzhirkevich.customqrgenerator.vector.style

import androidx.annotation.FloatRange
import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Style of the qr-code eye internal ball.
 * */
interface QrVectorBallShape : QrVectorShapeModifier {

    @Serializable
    @SerialName("Default")
    object Default : QrVectorBallShape, QrVectorShapeModifier by DefaultVectorShape

    @Serializable
    @SerialName("Circle")
    data class Circle(
        @FloatRange(from = 0.0, to = 1.0) val size: Float
    ) : QrVectorBallShape, QrVectorShapeModifier by CircleVectorShape(size)

    @Serializable
    @SerialName("RoundCorners")
    data class RoundCorners(
        @FloatRange(from = 0.0, to = .5) val radius: Float
    ) : QrVectorBallShape, QrVectorShapeModifier by RoundCornersVectorShape(radius)

    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        @Suppress("unchecked_cast")
        override val defaultSerializersModule: SerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModule {
                polymorphicDefaultSerializer(QrVectorBallShape::class){
                    Default.serializer() as SerializationStrategy<QrVectorBallShape>
                }
                polymorphicDefaultDeserializer(QrVectorBallShape::class) {
                    Default.serializer()
                }
                polymorphic(QrVectorBallShape::class){
                    subclass(Default::class)
                    subclass(Circle::class)
                    subclass(RoundCorners::class)
                }
            }
        }
    }
}