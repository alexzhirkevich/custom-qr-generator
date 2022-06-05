package com.github.alexzhirkevich.customqrgenerator.style

class QrStyle(
    val pixel : QrPixelStyle = QrPixelStyle.Default,
    val frame : QrFrameStyle = QrFrameStyle.Default,
    val ball : QrBallStyle = QrBallStyle.Default,
    val bgShape : QrBackgroundStyle = QrBackgroundStyle.Default,
    val qrShape : QrShape = QrShape.Default
)