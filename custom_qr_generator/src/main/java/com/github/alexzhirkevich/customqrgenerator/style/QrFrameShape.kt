package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange
import kotlin.math.sqrt

/**
 * Style of the qr-code eye frame.
 * (changing has no affect).
 * You can implement your own style by overriding [invoke] method.
 * Frame width should be equal to pixelSize.
 * @see QrShapeModifier
 * */
interface QrFrameShape : QrShapeModifier<Boolean> {

    object Default : QrFrameShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean {
            val size = elementSize
            return i in 0..qrPixelSize || j in 0..qrPixelSize ||
                    i in size-qrPixelSize..size || j in size- qrPixelSize .. size
        }
    }

    /**
     * Special style for QR code eye frame - frame pixels will be counted as qr pixels.
     * For example, [QrPixelShape.Circle] style will make eye frame look like a chaplet.
     * */
    class AsPixelShape(override val delegate: QrPixelShape)
        : QrFrameShape, ModifierDelegate<Boolean, QrPixelShape> {
        @Throws(IllegalStateException::class)
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean {
            return Default.invoke(i, j, elementSize, qrPixelSize, neighbors) &&
                    delegate.invoke(i % qrPixelSize, j % qrPixelSize,
                        qrPixelSize, qrPixelSize, neighbors)
        }
    }

    object Circle : QrFrameShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean {
            val radius = elementSize / 2.0
            return sqrt((radius - i) * (radius - i) + (radius - j) * (radius - j)) in
                    radius - qrPixelSize .. radius
        }
    }

    class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrFrameShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean {
            val cornerRadius = (.5f - corner.coerceIn(0f, .5f)) * elementSize
            val center = elementSize/2f

            val sub = center - cornerRadius
            val sum = center + cornerRadius

            val (x,y) = when{
                outer && i < sub && j < sub -> sub to sub
                horizontalOuter && i < sub && j > sum -> sub to sum
                verticalOuter && i > sum && j < sub -> sum to sub
                inner && i > sum && j > sum -> sum to sum
                else -> return Default.invoke(i, j,elementSize,qrPixelSize,neighbors)
            }
            return sqrt((x-i)*(x-i) + (y-j)*(y-j)) in sub-qrPixelSize .. sub
        }
    }
}