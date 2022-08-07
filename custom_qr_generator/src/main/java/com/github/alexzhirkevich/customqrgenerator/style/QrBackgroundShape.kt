package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange

interface QrBackgroundShape : QrShapeModifier {

    object Default : QrShapeModifierDelegate(
        delegate = DefaultShapeModifier,
    ), QrBackgroundShape


    object Circle : QrShapeModifierDelegate(
        delegate = QrPixelShape.Circle(1f)
    ), QrBackgroundShape


    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val  outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrShapeModifierDelegate(
        delegate = QrBallShape.RoundCorners(
            corner, outer, horizontalOuter, verticalOuter, inner)
    ), QrBackgroundShape
}

fun QrShapeModifier.asBackgroundShape() : QrBackgroundShape = if (this is QrBackgroundShape) this else
    object : QrBackgroundShape, QrShapeModifier by this{}