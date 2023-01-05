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

@Deprecated("Use QrCodeDrawable instead")
fun interface QrHighlightingShape : QrShapeModifier {

    @Serializable
    @SerialName("Default")
    @Deprecated("Use QrCodeDrawable instead")
    object Default : QrHighlightingShape by DefaultShapeModifier
        .asHighlightingShape()


    @Serializable
    @SerialName("Circle")
    @Deprecated("Use QrCodeDrawable instead")
    object Circle : QrHighlightingShape by CircleShapeModifier(1f)
        .asHighlightingShape()


    @Serializable
    @SerialName("RoundCorners")
    @Deprecated("Use QrCodeDrawable instead")
    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val  outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrHighlightingShape by RoundCornersShapeModifier(
        corner, false, outer, horizontalOuter, verticalOuter, inner
    ).asHighlightingShape()

    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        @Suppress("unchecked_cast")
        override val defaultSerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModule {
                polymorphicDefaultSerializer(QrHighlightingShape::class){
                    Default.serializer() as SerializationStrategy<QrHighlightingShape>
                }
                polymorphicDefaultDeserializer(QrHighlightingShape::class) {
                    Default.serializer()
                }
                polymorphic(QrHighlightingShape::class) {
                    subclass(Default::class)
                    subclass(Circle::class)
                    subclass(RoundCorners::class)
                }
            }
        }
    }
}

@Deprecated("Use QrCodeDrawable instead")
fun QrShapeModifier.asHighlightingShape() : QrHighlightingShape = if (this is QrHighlightingShape) this else
    QrHighlightingShape { i, j, elementSize, neighbors ->
        this@asHighlightingShape
            .invoke(i, j, elementSize, neighbors)
    }