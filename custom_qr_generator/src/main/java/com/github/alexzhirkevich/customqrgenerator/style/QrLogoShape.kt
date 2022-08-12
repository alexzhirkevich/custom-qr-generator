package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange

/**
 * Shape of the qr-code logo padding.
 * */
interface QrLogoShape : QrShapeModifier {

    object Default : QrShapeModifierDelegate(
        delegate = DefaultShapeModifier
    ), QrLogoShape

    object Circle : QrShapeModifierDelegate(
        delegate = QrBallShape.Circle(1f)
    ), QrLogoShape

    object Rhombus : QrShapeModifierDelegate(
        delegate = QrBallShape.Rhombus
    ), QrLogoShape

    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrShapeModifierDelegate(
       delegate =  QrBallShape.RoundCorners(
           corner, outer, horizontalOuter, verticalOuter, inner)
    ), QrLogoShape
}

fun QrShapeModifier.asLogoShape() : QrLogoShape = object : QrLogoShape {
    override fun invoke(
        i: Int, j: Int, elementSize: Int,
        neighbors: Neighbors
    ): Boolean = this@asLogoShape
        .invoke(i, j, elementSize, neighbors)

}

internal object UndefinedLogoShape : QrLogoShape {
    override fun invoke(
        i: Int, j: Int, elementSize: Int, neighbors: Neighbors
    ): Boolean = true
}