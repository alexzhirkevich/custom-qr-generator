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
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Style of the qr-code eye frame.
 *
 * Frame width should be equal to elementSize/7.
 * */
fun interface QrFrameShape : QrShapeModifier {

    @Serializable
    @SerialName("Default")
    object Default : QrFrameShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int, neighbors: Neighbors
        ): Boolean {
            val qrPixelSize = elementSize/7
            return i in 0..qrPixelSize || j in 0..qrPixelSize ||
                    i in elementSize-qrPixelSize..elementSize ||
                    j in elementSize - qrPixelSize .. elementSize
        }
    }


    /**
     * Special style for QR code eye frame - frame pixels will be counted as qr pixels.
     * For example, [QrPixelShape.Circle] style will make eye frame look like a chaplet.
     * */
    @Serializable
    @SerialName("AsPixelShape")
    data class AsPixelShape(val shape: QrPixelShape) : QrFrameShape by
        (Default.and(shape % { size, _, -> size /7})).asFrameShape()


    @Serializable
    @SerialName("Circle")
    object Circle : QrFrameShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int, neighbors: Neighbors
        ): Boolean {
            val radius = elementSize / 2.0
            val qrPixelSize = elementSize/7

            return sqrt((radius - i).pow(2) + (radius - j).pow(2)) in
                    radius - qrPixelSize .. radius
        }
    }


    @Serializable
    @SerialName("RoundCorners")
    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrFrameShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int, neighbors: Neighbors
        ): Boolean {
            val cornerRadius = (.5f - corner.coerceIn(0f, .5f)) * elementSize
            val center = elementSize/2f
            val qrPixelSize = elementSize/7

            val sub = center - cornerRadius
            val sum = center + cornerRadius


            val (x,y) = when{
                outer && i < sub && j < sub -> sub to sub
                horizontalOuter && i < sub && j > sum -> sub to sum
                verticalOuter && i > sum && j < sub -> sum to sub
                inner && i > sum && j > sum -> sum to sum
                else -> return Default.invoke(i, j, elementSize, neighbors)
            }
            return sqrt((x-i)*(x-i) + (y-j)*(y-j)) in sub-qrPixelSize .. sub
        }
    }

    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        @Suppress("unchecked_cast")
        override val defaultSerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModule {
                polymorphicDefaultSerializer(QrFrameShape::class){
                    Default.serializer() as SerializationStrategy<QrFrameShape>
                }
                polymorphicDefaultDeserializer(QrFrameShape::class) {
                    Default.serializer()
                }
                polymorphic(QrFrameShape::class) {
                    subclass(Default::class)
                    subclass(AsPixelShape::class)
                    subclass(Circle::class)
                    subclass(RoundCorners::class)
                }
            }
        }
    }
}

fun QrShape.Companion.pizedc(){

}

fun QrShapeModifier.asFrameShape() : QrFrameShape = if (this is QrFrameShape) this else
    QrFrameShape { i, j, elementSize, neighbors ->
        this@asFrameShape
            .invoke(i, j, elementSize, neighbors)
    }