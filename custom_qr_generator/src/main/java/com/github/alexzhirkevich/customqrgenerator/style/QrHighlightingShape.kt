package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange

interface QrHighlightingShape : QrShapeModifier {

    object Default : QrShapeModifierDelegate(
        delegate = DefaultShapeModifier,
    ), QrHighlightingShape


    object Circle : QrShapeModifierDelegate(
        delegate = QrPixelShape.Circle(1f)
    ), QrHighlightingShape


    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val  outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrShapeModifierDelegate(
        delegate = QrBallShape.RoundCorners(
            corner, outer, horizontalOuter, verticalOuter, inner)
    ), QrHighlightingShape
}

fun QrShapeModifier.asHighlightingShape() : QrHighlightingShape = if (this is QrHighlightingShape) this else
    object : QrHighlightingShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            neighbors: Neighbors
        ): Boolean = this@asHighlightingShape
            .invoke(i, j, elementSize, neighbors)
    }