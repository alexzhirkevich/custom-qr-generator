package com.github.alexzhirkevich.customqrgenerator.style

import android.graphics.Color

/**
 * @param light color of light QR code dots
 * @param dark color of dark QR code dots
 * @param frame color of code eyes frame
 * @param ball color of code eyes ball
 * @param bitmapBackground color of bitmap background (behind image)
 * @param codeBackground color of code background (above image; after paddings)
 * Shape regulated by [QrElementsShapes.background]
 * */
data class QrColors(
    val light : QrColor = QrColor.Unspecified,
    val dark : QrColor = QrColor.Solid(Color.BLACK),
    val frame : QrColor = QrColor.Unspecified,
    val ball : QrColor = QrColor.Unspecified,
    val bitmapBackground : QrColor = QrColor.Solid(Color.WHITE),
    val codeBackground : QrColor = QrColor.Unspecified,
)