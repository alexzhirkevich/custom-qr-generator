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

interface QrVectorLogoShape : QrVectorShapeModifier {

    @Serializable
    @SerialName("Default")
    object Default : QrVectorLogoShape, QrVectorShapeModifier by DefaultVectorShape

    @Serializable
    @SerialName("Circle")
    object Circle : QrVectorLogoShape, QrVectorShapeModifier by CircleVectorShape(1f)

    @Serializable
    @SerialName("RoundCorners")
    data class RoundCorners(
        @FloatRange(from = 0.0, to = .5) val radius: Float
    ) : QrVectorLogoShape, QrVectorShapeModifier by RoundCornersVectorShape(radius, false)

    @Serializable
    @SerialName("Rhombus")
    object Rhombus : QrVectorLogoShape, QrVectorShapeModifier by RhombusVectorShape(1f)

    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        @Suppress("unchecked_cast")
        override val defaultSerializersModule: SerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModule {
                polymorphicDefaultSerializer(QrVectorLogoShape::class){
                    Default.serializer() as SerializationStrategy<QrVectorLogoShape>
                }
                polymorphicDefaultDeserializer(QrVectorLogoShape::class) {
                    Default.serializer()
                }
                polymorphic(QrVectorLogoShape::class){
                    subclass(Default::class)
                    subclass(Circle::class)
                    subclass(RoundCorners::class)
                    subclass(Rhombus::class)
                }
            }
        }
    }
}