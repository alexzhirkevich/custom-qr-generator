@file:Suppress("deprecation")

package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange

/**
 * Shape of the qr-code logo padding.
 * */
@Deprecated("Use QrCodeDrawable with QrVectorLogoShape instead")
fun interface QrLogoShape : QrShapeModifier {


    @Deprecated("Use QrCodeDrawable with QrVectorLogoShape instead")
    object Default : QrLogoShape by DefaultShapeModifier
        .asLogoShape()

    @Deprecated("Use QrCodeDrawable with QrVectorLogoShape instead")
    object Circle : QrLogoShape by CircleShapeModifier(1f)
        .asLogoShape()


    @Deprecated("Use QrCodeDrawable with QrVectorLogoShape instead")
    object Rhombus : QrLogoShape by RhombusShapeModifier
        .asLogoShape()


    @Deprecated("Use QrCodeDrawable with QrVectorLogoShape instead")
    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrLogoShape by RoundCornersShapeModifier(
        corner, false, outer, horizontalOuter, verticalOuter, inner
    ).asLogoShape()
}
@Deprecated("Use QrCodeDrawable with QrVectorLogoShape instead")
fun QrShapeModifier.asLogoShape() : QrLogoShape =
    QrLogoShape { i, j, elementSize, neighbors ->
        this@asLogoShape
            .invoke(i, j, elementSize, neighbors)
    }

