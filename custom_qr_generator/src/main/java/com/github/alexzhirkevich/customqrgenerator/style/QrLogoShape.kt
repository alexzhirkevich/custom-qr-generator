package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange

/**
 * Shape of the qr-code logo padding.
 * You can implement your own shape by overriding [invoke] method.
 * */
interface QrLogoShape : QrShapeModifier<Boolean> {

    object Default : QrLogoShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean = true
    }

    object Circle : QrLogoShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean = QrBallShape.Circle(1f)
            .invoke(i, j,elementSize, qrPixelSize, neighbors)
    }

    object Rhombus : QrLogoShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean {
            return QrBallShape.Rhombus
                .invoke(i, j,elementSize, qrPixelSize, neighbors)
        }
    }

    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrLogoShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean = QrBallShape.RoundCorners(corner, outer, horizontalOuter, verticalOuter, inner)
            .invoke(i, j,elementSize, qrPixelSize, neighbors)
    }
}