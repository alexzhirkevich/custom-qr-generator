package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange
import kotlin.math.sqrt

/**
 * Style of the qr-code eye frame.
 * Element size in 7 by default, but 3 middle units are reserved for [QrBallStyle]
 * (changing has no affect).
 * You can implement your own style by overriding [isDark] method.
 * Frame width should be equal to pixelSize.
 * @see QrModifier
 * */
interface QrFrameStyle : QrModifier {

    object Default : QrFrameStyle {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int,
            neighbors: Neighbors
        ): Boolean {
            val size = elementSize
            return i in 0..qrPixelSize || j in 0..qrPixelSize ||
                    i in size-qrPixelSize..size || j in size- qrPixelSize .. size
        }
    }

    /**
     * Special style for QR code eye frame - frame pixels will be counted as qr pixels.
     * For example, [QrPixelStyle.Circle] style will make eye frame look like a chaplet.
     * */
    class AsPixelsStyle(override val pixelStyle: QrPixelStyle) : QrFrameStyle, AsPixels {
        @Throws(IllegalStateException::class)
        override fun isDark(
            i: Int,
            j: Int,
            elementSize: Int,
            qrPixelSize: Int,
            neighbors: Neighbors
        ): Nothing {
            throw IllegalStateException("AsPixels style delegates it's work and could not be called")
        }
    }

    object Circle : QrFrameStyle {
        override fun isDark(
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
    ) : QrFrameStyle {
        override fun isDark(
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
                else -> return Default.isDark(i, j,elementSize,qrPixelSize,neighbors)
            }
            return sqrt((x-i)*(x-i) + (y-j)*(y-j)) in sub-qrPixelSize .. sub
        }
    }
}