package com.github.alexzhirkevich.customqrgenerator

import androidx.annotation.FloatRange
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorShapeModifier


interface IAnchorsHighlighting {
    val cornerEyes : HighlightingType
    val versionEyes : HighlightingType
    val timingLines : HighlightingType
    val alpha : Float
}



/**
 * Highlighting of the anchor QR code elements.
 * Has the most impact when using a background image or color
 *
 * @param cornerEyes background highlighting of the corner eyes
 * @param versionEyes background and draw mode of version eyes
 * @param timingLines vertical and horizontal lines of interleaved pixels
 * */
data class QrHighlighting(
    override val cornerEyes : HighlightingType = HighlightingType.None,
    override val versionEyes : HighlightingType = HighlightingType.None,
    override val timingLines : HighlightingType = HighlightingType.None,
    @FloatRange(from = 0.0, to = 1.0) override val alpha: Float = .75f
) : IAnchorsHighlighting



sealed interface HighlightingType {

    object None : HighlightingType

    object Default : HighlightingType

    class Styled(
        val shape : QrVectorShapeModifier? = null,
        val color : QrVectorColor? = null
    ) : HighlightingType
}