package com.github.alexzhirkevich.customqrgenerator.style

class QrStyle(
    val pixel : QrPixelStyle = QrPixelStyle.Default,
    val frame : QrFrameStyle = QrFrameStyle.Default,
    val ball : QrBallStyle = QrBallStyle.Default,
    val shape : QrShape = QrShape.Default
)