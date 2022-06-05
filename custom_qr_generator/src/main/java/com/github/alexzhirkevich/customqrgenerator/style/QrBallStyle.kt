package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange
import kotlin.math.sqrt

/**
 * Style of the qr-code eye internal ball.
 * Element size is 3 by default.
 * You can implement your own style by overriding [isDark] method.
 * @see QrModifier
 * */
interface QrBallStyle : QrModifier {

    object Default : QrBallStyle

    /**
     * Special style for QR code ball - ball pixels will be counted as qr pixels.
     * For example, [QrPixelStyle.Circle] style will make qr-code ball look like a square of 9 balls.
     * */
    class AsPixelsStyle(override val pixelStyle: QrPixelStyle) : QrBallStyle, AsPixels {
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


    class Circle(@FloatRange(from = .75, to = 1.0) private val size : Float = 1f) : QrBallStyle {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int,
            neighbors: Neighbors
        ): Boolean = QrPixelStyle.Circle(size)
            .isDark(i, j,elementSize, qrPixelSize, neighbors)
    }
    object Rhombus : QrBallStyle {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int,
            neighbors: Neighbors
        ): Boolean =
            QrPixelStyle.Rhombus
                .isDark(i, j,elementSize, qrPixelSize, neighbors)

    }

    class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrBallStyle {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int,
            neighbors: Neighbors
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
                else -> return QrLogoShape.Default
                    .isDark(i, j,elementSize, qrPixelSize, neighbors)
            }
            return sqrt((x-i)*(x-i) + (y-j)*(y-j)) < sub
        }
    }
}