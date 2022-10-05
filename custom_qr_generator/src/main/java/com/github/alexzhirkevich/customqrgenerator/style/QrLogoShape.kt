package com.github.alexzhirkevich.customqrgenerator.style

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
 * Shape of the qr-code logo padding.
 * */
fun interface QrLogoShape : QrShapeModifier {

    @Serializable
    @SerialName("Default")
    object Default : QrLogoShape by DefaultShapeModifier
        .asLogoShape()


    @Serializable
    @SerialName("Circle")
    object Circle : QrLogoShape by CircleShapeModifier(1f)
        .asLogoShape()


    @Serializable
    @SerialName("Rhombus")
    object Rhombus : QrLogoShape by RhombusShapeModifier
        .asLogoShape()


    @Serializable
    @SerialName("RoundCorners")
    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrLogoShape by RoundCornersShapeModifier(
        corner, false, outer, horizontalOuter, verticalOuter, inner
    ).asLogoShape()


    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        @Suppress("unchecked_cast")
        override val defaultSerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModule {
                polymorphicDefaultSerializer(QrLogoShape::class){
                    Default.serializer() as SerializationStrategy<QrLogoShape>
                }
                polymorphicDefaultDeserializer(QrLogoShape::class) {
                    Default.serializer()
                }
                polymorphic(QrLogoShape::class) {
                    subclass(Default::class)
                    subclass(Circle::class)
                    subclass(Rhombus::class)
                    subclass(RoundCorners::class)
                }
            }
        }
    }
}

fun QrShapeModifier.asLogoShape() : QrLogoShape =
    QrLogoShape { i, j, elementSize, neighbors ->
        this@asLogoShape
            .invoke(i, j, elementSize, neighbors)
    }

