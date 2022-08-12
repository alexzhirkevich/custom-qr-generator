package com.github.alexzhirkevich.customqrgenerator.style

/**
 * @property darkPixel shape of dark pixels
 * @property lightPixel shape of light pixels
 * @property frame shape of qr-code eye frames
 * @property ball shape of qr-code eye balls
 * @property hightlighting shape of qr-code background without side paddings.
 * [QrColors.highlighting] will be applied according to this shape
 * */
data class QrElementsShapes(
    val darkPixel : QrPixelShape = QrPixelShape.Default,
    val lightPixel : QrPixelShape = QrPixelShape.Default,
    val frame : QrFrameShape = QrFrameShape.Default,
    val ball : QrBallShape = QrBallShape.Default,
    val hightlighting : QrBackgroundShape = QrBackgroundShape.Default,
)