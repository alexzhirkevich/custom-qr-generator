@file:Suppress("deprecation")

package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Style of the qr-code eye frame.
 *
 * Frame width should be equal to elementSize/7.
 * */
@Deprecated("Use QrCodeDrawable with QrVectorFrameShape instead")
fun interface QrFrameShape : QrShapeModifier {

    
    @Deprecated("Use QrCodeDrawable with QrVectorFrameShape instead")
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
     * Special style for QR code frame.
     *
     * [AsPixelShape] with the shape of dark pixels will be used.
     * */
    
    @Deprecated("Use QrCodeDrawable with QrVectorFrameShape instead")
    object AsDarkPixels : QrFrameShape {
        override fun invoke(i: Int, j: Int, elementSize: Int, neighbors: Neighbors): Boolean = false
    }


    /**
     * Special style for QR code eye frame - frame pixels will be counted as qr pixels.
     * For example, [QrPixelShape.Circle] style will make eye frame look like a chaplet.
     *
     * Used pixel shape will not depend on [Neighbors]
     * */
    
    @Deprecated("Use QrCodeDrawable with QrVectorFrameShape instead")
    data class AsPixelShape(val shape: QrPixelShape) : QrFrameShape by
        (Default.and(shape % { size, _, -> size /7})).asFrameShape()


    
    @Deprecated("Use QrCodeDrawable with QrVectorFrameShape instead")
    class Circle(
        @FloatRange(from = 0.0) val width : Float = 1f,
        @FloatRange(from = 0.0) val radius : Float = 1f
        ) : QrFrameShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int, neighbors: Neighbors
        ): Boolean {
            val center = elementSize/2f
            val scaledRadius = center * radius
            val qrPixelSize = elementSize/7 * width
                .coerceAtLeast(0f)

            return sqrt((center - i).pow(2) + (center - j).pow(2)) in
                    scaledRadius - qrPixelSize .. scaledRadius
        }
    }


    
    @Deprecated("Use QrCodeDrawable with QrVectorFrameShape instead")
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
}

@Deprecated("Use QrCodeDrawable with QrVectorFrameShape instead")
fun QrShapeModifier.asFrameShape() : QrFrameShape = if (this is QrFrameShape) this else
    QrFrameShape { i, j, elementSize, neighbors ->
        this@asFrameShape
            .invoke(i, j, elementSize, neighbors)
    }