package com.github.alexzhirkevich.customqrgenerator.vector.style

interface IQrVectorShapes{
    val darkPixel: QrVectorPixelShape
    val lightPixel : QrVectorPixelShape
    val ball : QrVectorBallShape
    val frame : QrVectorFrameShape
    val centralSymmetry : Boolean
}

/**
 * Shapes of QR code elements
 * */

data class QrVectorShapes(
    override val darkPixel: QrVectorPixelShape = QrVectorPixelShape.Default,
    override val lightPixel : QrVectorPixelShape = QrVectorPixelShape.Default,
    override val ball : QrVectorBallShape = QrVectorBallShape.Default,
    override val frame : QrVectorFrameShape = QrVectorFrameShape.Default,
    override val centralSymmetry: Boolean = true
) : IQrVectorShapes