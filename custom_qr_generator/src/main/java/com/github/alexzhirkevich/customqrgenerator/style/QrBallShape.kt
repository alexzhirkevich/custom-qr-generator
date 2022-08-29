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
 * Style of the qr-code eye internal ball.
 * */
fun interface QrBallShape : QrShapeModifier {

    @Serializable
    @SerialName("Default")
    object Default : QrBallShape by DefaultShapeModifier.asBallShape()


    /**
     * Special style for QR code ball - ball pixels will be counted as qr pixels.
     * For example, [QrPixelShape.Circle] style will make qr-code ball look like a square of 9 balls.
     * */
    @Serializable
    @SerialName("AsPixelShape")
    data class AsPixelShape(val shape: QrPixelShape) : QrBallShape by
        (Default.and(shape % { size, _ -> size/3 })).asBallShape()


    @Serializable
    @SerialName("Circle")
    data class Circle(
        @FloatRange(from = .75, to = 1.0)
        private val size : Float = 1f
    ) : QrBallShape by CircleShapeModifier(size)
        .asBallShape()


    @Serializable
    @SerialName("Rhombus")
    object Rhombus : QrBallShape by RhombusShapeModifier
        .asBallShape()


    @Serializable
    @SerialName("RoundCorners")
    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrBallShape by RoundCornersShapeModifier(
        corner,false, outer,horizontalOuter,verticalOuter,inner
    ).asBallShape()


    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        @Suppress("unchecked_cast")
        override val defaultSerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModule {
                include(QrPixelShape.defaultSerializersModule)

                polymorphicDefaultSerializer(QrBallShape::class){
                    Default.serializer() as SerializationStrategy<QrBallShape>
                }
                polymorphicDefaultDeserializer(QrBallShape::class) {
                    Default.serializer()
                }
                polymorphic(QrBallShape::class) {
                    subclass(Default::class)
                    subclass(AsPixelShape::class)
                    subclass(Circle::class)
                    subclass(Rhombus::class)
                    subclass(RoundCorners::class)
                }
            }
        }
    }
}

fun QrShapeModifier.asBallShape() : QrBallShape = if (this is QrBallShape) this else
    QrBallShape { i, j, elementSize, neighbors ->
        this@asBallShape
            .invoke(i, j, elementSize, neighbors)
    }