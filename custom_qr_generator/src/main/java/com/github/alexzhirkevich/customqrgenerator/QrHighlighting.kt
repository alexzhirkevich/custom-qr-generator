package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Color
import androidx.annotation.FloatRange
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorShapeModifier
import com.github.alexzhirkevich.customqrgenerator.vector.style.isSpecified
import com.github.alexzhirkevich.customqrgenerator.vector.style.plus
import com.github.alexzhirkevich.customqrgenerator.vector.style.scale


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

/**
 * Create eye shepe for styled [QrHighlighting.versionEyes].
 *
 * @param frame must be created with [QrVectorFrameShape.size] = 5
 * @param ball with any size
 * */
fun QrVersionEyeShape(
    frame : QrVectorFrameShape = QrVectorFrameShape.Default,
    ball : QrVectorBallShape = QrVectorBallShape.Default
) : QrVectorShapeModifier {
    require(frame.size == 5){
        "Frame for QrVersionEyeShape must be created with the size = 5"
    }
    return frame + ball.scale(1 / 5f)
}
    
sealed interface HighlightingType {

    object None : HighlightingType

    object Default : HighlightingType

    /**
     * @param shape shape of the highlighting
     * @param color color of the highlighting
     * @param elementShape shape of the element being highlighted
     * @param elementColor color of the element being highlighted
     *
     * @see QrVersionEyeShape
     * */
    class Styled(
        val shape : QrVectorShapeModifier = QrVectorPixelShape.Default,
        val color : QrVectorColor = QrVectorColor.Solid(Color.WHITE),
        val elementShape : QrVectorShapeModifier? = null,
        val elementColor : QrVectorColor = QrVectorColor.Solid(Color.BLACK),
    ) : HighlightingType
}



internal val HighlightingType.color : QrVectorColor?
    get() = (this as? HighlightingType.Styled)?.color?.takeIf { it.isSpecified }

internal val HighlightingType.shape : QrVectorShapeModifier
    get() = (this as? HighlightingType.Styled)?.shape ?: QrVectorPixelShape.Default
internal val HighlightingType.elementColor : QrVectorColor?
    get() = (this as? HighlightingType.Styled)?.elementColor?.takeIf { it.isSpecified }

internal val HighlightingType.elementShape : QrVectorShapeModifier?
    get() = (this as? HighlightingType.Styled)?.elementShape

internal val HighlightingType.isStyledWithElShape : Boolean
    get() = (this as? HighlightingType.Styled)?.elementShape != null

internal val HighlightingType.isStyledWithElColor : Boolean
    get() = (this as? HighlightingType.Styled)?.elementColor?.isSpecified == true
