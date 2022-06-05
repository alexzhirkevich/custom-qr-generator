package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange

interface QrBackgroundStyle : QrModifier {

    object Default : QrBackgroundStyle

    object Circle : QrBackgroundStyle{
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean {
            return QrPixelStyle.Circle(1f).isDark(i, j, elementSize, qrPixelSize, neighbors)
        }
    }
    class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrBackgroundStyle {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean = QrBallStyle.RoundCorners(corner, outer, horizontalOuter, verticalOuter, inner)
            .isDark(i, j,elementSize, qrPixelSize, neighbors)
    }
}