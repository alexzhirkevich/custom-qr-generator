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
            pixelSize: Int, neighbors: Neighbors
        ): Boolean {
            val size = elementSize * pixelSize
            return i in 0..pixelSize || j in 0..pixelSize ||
                    i in size-pixelSize..size || j in size- pixelSize .. size
        }
    }

    object Circle : QrFrameStyle {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            pixelSize: Int, neighbors: Neighbors
        ): Boolean {
            val radius = elementSize * pixelSize / 2.0
            return sqrt((radius - i) * (radius - i) + (radius - j) * (radius - j)) in
                    radius - pixelSize .. radius
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
                else -> return Default.isDark(i, j, elementSize, pixelSize, neighbors)
            }
            return sqrt((x-i)*(x-i) + (y-j)*(y-j)) in sub-pixelSize .. sub
        }
    }
}