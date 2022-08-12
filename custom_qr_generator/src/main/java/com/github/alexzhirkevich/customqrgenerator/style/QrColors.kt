package com.github.alexzhirkevich.customqrgenerator.style

import android.graphics.Color

/**
 * @property light color of light QR code dots
 * @property dark color of dark QR code dots
 * @property frame color of code eyes frame
 * @property ball color of code eyes ball
 * @property bitmapBackground color of bitmap background (behind image)
 * @property highlighting color of code background (above image, after paddings)
 * Shape regulated by [QrElementsShapes.hightlighting]
 * */
data class QrColors(
    val light : QrColor = QrColor.Unspecified,
    val dark : QrColor = QrColor.Solid(Color.BLACK),
    val frame : QrColor = QrColor.Unspecified,
    val ball : QrColor = QrColor.Unspecified,
    val bitmapBackground : QrColor = QrColor.Solid(Color.WHITE),
    val highlighting : QrColor = QrColor.Unspecified,
)