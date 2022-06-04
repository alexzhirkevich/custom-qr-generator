package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange

interface QrShape : QrModifier {

    object Default : QrShape

    class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrShape {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            pixelSize: Int, neighbors: Neighbors
        ): Boolean = QrBallStyle.RoundCorners(corner, outer, horizontalOuter, verticalOuter, inner)
            .isDark(i, j, elementSize, pixelSize, neighbors)
    }
}