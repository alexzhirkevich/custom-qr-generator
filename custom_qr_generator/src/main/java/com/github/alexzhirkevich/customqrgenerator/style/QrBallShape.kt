@file:Suppress("deprecation")

package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange

/**
 * Style of the qr-code eye internal ball.
 * */
@Deprecated("Use QrCodeDrawable with QrVectorBallShape instead")
fun interface QrBallShape : QrShapeModifier {

    
    @Deprecated("Use QrCodeDrawable with QrVectorBallShape instead")
    object Default : QrBallShape by DefaultShapeModifier.asBallShape()

    /**
     * Special style for QR code ball.
     *
     * [AsPixelShape] with the shape of dark pixels will be used.
     *
     *
     * */
    
    @Deprecated("Use QrCodeDrawable with QrVectorBallShape instead")
    object AsDarkPixels : QrBallShape {
        override fun invoke(i: Int, j: Int, elementSize: Int, neighbors: Neighbors): Boolean = false
    }

    /**
     * Special style for QR code ball - ball pixels will be counted as qr pixels.
     * For example, [QrPixelShape.Circle] style will make qr-code ball look like a square of 9 balls.
     *
     * Used pixel shape will not depend on [Neighbors]
     * */
    
    @Deprecated("Use QrCodeDrawable with QrVectorBallShape instead")
    data class AsPixelShape(val shape: QrPixelShape) : QrBallShape by
        (Default.and(shape % { size, _ -> size/3 })).asBallShape()


    /**
     * @property size size of circle. Should be from .75 to 1.
     * Otherwise, QR code can be unreadable
     * */
    
    @Deprecated("Use QrCodeDrawable with QrVectorBallShape instead")
    data class Circle(
        @FloatRange(from = .75, to = 1.0)
        private val size : Float = 1f
    ) : QrBallShape by CircleShapeModifier(size)
        .asBallShape()


    
    @Deprecated("Use QrCodeDrawable with QrVectorBallShape instead")
    object Rhombus : QrBallShape by RhombusShapeModifier
        .asBallShape()


    
    @Deprecated("Use QrCodeDrawable with QrVectorBallShape instead")
    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrBallShape by RoundCornersShapeModifier(
        corner,false, outer,horizontalOuter,verticalOuter,inner
    ).asBallShape()

}

@Deprecated("Use QrCodeDrawable with QrVectorBallShape instead")
fun QrShapeModifier.asBallShape() : QrBallShape = if (this is QrBallShape) this else
    QrBallShape { i, j, elementSize, neighbors ->
        this@asBallShape
            .invoke(i, j, elementSize, neighbors)
    }