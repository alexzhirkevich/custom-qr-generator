package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange

/**
 * Style of the qr-code logo padding.
 * You can implement your own style by overriding [isDark] method.
 * @see QrModifier
 * */
interface QrLogoShape : QrModifier {

    object Default : QrLogoShape

    object Circle : QrLogoShape {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            pixelSize: Int, neighbors: Neighbors
        ): Boolean = QrBallStyle.Circle.isDark(i, j, elementSize, pixelSize, neighbors)
    }

    object Rhombus : QrLogoShape {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            pixelSize: Int, neighbors: Neighbors
        ): Boolean {
            return QrBallStyle.Rhombus.isDark(i, j, elementSize, pixelSize, neighbors)

        }
    }

    class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrLogoShape {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            pixelSize: Int, neighbors: Neighbors
        ): Boolean = QrBallStyle.RoundCorners(corner, outer, horizontalOuter, verticalOuter, inner)
            .isDark(i, j, elementSize, pixelSize, neighbors)
    }
}