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

    object Circle : QrBallStyle {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            pixelSize: Int, neighbors: Neighbors
        ): Boolean = QrPixelStyle.Circle.isDark(i, j, elementSize, pixelSize, neighbors)
    }
    object Rhombus : QrBallStyle {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            pixelSize: Int, neighbors: Neighbors
        ): Boolean {
            return QrPixelStyle.Rhombus.isDark(i, j, elementSize, pixelSize, neighbors)
        }
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
            pixelSize: Int, neighbors: Neighbors
        ): Boolean {
            val cornerRadius = (.5f - corner.coerceIn(0f, .5f)) * elementSize * pixelSize
            val center = elementSize*pixelSize/2f

            val sub = center - cornerRadius
            val sum = center + cornerRadius

            val (x,y) = when{
                outer && i < sub && j < sub -> sub to sub
                horizontalOuter && i < sub && j > sum -> sub to sum
                verticalOuter && i > sum && j < sub -> sum to sub
                inner && i > sum && j > sum -> sum to sum
                else -> return QrLogoShape.Default.isDark(i, j, elementSize, pixelSize, neighbors)
            }
            return sqrt((x-i)*(x-i) + (y-j)*(y-j)) < sub
        }
    }
}