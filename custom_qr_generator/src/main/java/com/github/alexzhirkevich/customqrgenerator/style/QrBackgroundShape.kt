package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange

interface QrBackgroundShape : QrShapeModifier<Boolean> {

    object Default : QrBackgroundShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean = true
    }

    object Circle : QrBackgroundShape{
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean {
            return QrPixelShape.Circle(1f)
                .invoke(i, j, elementSize, qrPixelSize, neighbors)
        }
    }
    class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrBackgroundShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean = QrBallShape.RoundCorners(corner, outer, horizontalOuter, verticalOuter, inner)
            .invoke(i, j,elementSize, qrPixelSize, neighbors)
    }
}