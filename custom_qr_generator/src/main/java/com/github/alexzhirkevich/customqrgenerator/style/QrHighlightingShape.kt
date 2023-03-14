@file:Suppress("deprecation")

package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange

@Deprecated("Use QrCodeDrawable instead")
fun interface QrHighlightingShape : QrShapeModifier {

    
    @Deprecated("Use QrCodeDrawable instead")
    object Default : QrHighlightingShape by DefaultShapeModifier
        .asHighlightingShape()


    
    @Deprecated("Use QrCodeDrawable instead")
    object Circle : QrHighlightingShape by CircleShapeModifier(1f)
        .asHighlightingShape()


    
    @Deprecated("Use QrCodeDrawable instead")
    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val  outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrHighlightingShape by RoundCornersShapeModifier(
        corner, false, outer, horizontalOuter, verticalOuter, inner
    ).asHighlightingShape()
}

@Deprecated("Use QrCodeDrawable instead")
fun QrShapeModifier.asHighlightingShape() : QrHighlightingShape = if (this is QrHighlightingShape) this else
    QrHighlightingShape { i, j, elementSize, neighbors ->
        this@asHighlightingShape
            .invoke(i, j, elementSize, neighbors)
    }