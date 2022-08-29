package com.github.alexzhirkevich.customqrgenerator.encoder

internal data class QrRenderResult(
    val bitMatrix: QrCodeMatrix,
    val paddingX : Int,
    val paddingY : Int,
    val pixelSize : Int,
    val shapeIncrease : Int,
    val frame : Rectangle,
    val ball : Rectangle,
    val error : Int,
)