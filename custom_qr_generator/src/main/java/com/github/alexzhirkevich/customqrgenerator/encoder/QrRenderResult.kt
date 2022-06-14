package com.github.alexzhirkevich.customqrgenerator.encoder

import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.encoder.ByteMatrix

internal data class QrRenderResult(
    val bitMatrix: ByteMatrix,
    val padding : Int,
    val pixelSize : Int,
    val shapeIncrease : Int,
    val frame : Rectangle,
    val ball : Rectangle,
    val error : Int,
)