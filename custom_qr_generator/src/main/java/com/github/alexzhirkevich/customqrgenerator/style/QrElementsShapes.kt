package com.github.alexzhirkevich.customqrgenerator.style

/**
 * @param darkPixel shape of dark pixels
 * @param lightPixel shape of light pixels
 * @param frame shape of qr-code eye frames
 * @param ball shape of qr-code eye balls
 * @param background shape of qr-code background without side paddings.
 * [QrColors.codeBackground] will be applied according this shape
 * @param code shape of all qr-code. [QrColors.codeBackground] will be applied according that shape
 * and shape of the
 * */
data class QrElementsShapes(
    val darkPixel : QrPixelShape = QrPixelShape.Default,
    val lightPixel : QrPixelShape = QrPixelShape.Default,
    val frame : QrFrameShape = QrFrameShape.Default,
    val ball : QrBallShape = QrBallShape.Default,
    val background : QrBackgroundShape = QrBackgroundShape.Default,
)