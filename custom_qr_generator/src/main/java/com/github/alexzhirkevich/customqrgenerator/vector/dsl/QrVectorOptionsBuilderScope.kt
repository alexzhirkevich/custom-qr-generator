package com.github.alexzhirkevich.customqrgenerator.vector.dsl

import com.github.alexzhirkevich.customqrgenerator.QrErrorCorrectionLevel
import com.github.alexzhirkevich.customqrgenerator.QrErrorCorrectionLevel.Auto
import com.github.alexzhirkevich.customqrgenerator.dsl.QrOffsetBuilderScope
import com.github.alexzhirkevich.customqrgenerator.style.DrawableSource
import com.github.alexzhirkevich.customqrgenerator.style.QrShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor

sealed interface QrVectorOptionsBuilderScope  {

    /**
     * Padding of the QR code pattern from drawable border.
     * Should be from 0 to 0.5. Default value is 0
     * */
    var padding: Float

    /**
     * Level of error correction.
     * Determines part of qr code that can be corrupted or used for a logo.
     * [Auto] by default
     * */
    var errorCorrectionLevel: QrErrorCorrectionLevel

    /**
     * Shape of the QR code pattert
     * */
    var codeShape : QrShape

    /**
     * Enable 4th qr code eye. False by default
     * */
    var fourthEyeEnabled : Boolean

    /**
     * Offset of the QR code pattern relative to padding size.
     * X and Y should be from -1 to 1. Both are 0 by default
     * */
    fun offset(x : Float, y : Float)

    /**
     * Shapes of QR code elements.
     * */
    fun shapes(centralSymmetry : Boolean = true, block: QrVectorShapesBuilderScope.() -> Unit)

    /**
     * Colors of QR code elements.
     * Background color is available in [background] section
     * */
    fun colors(block: QrVectorColorsBuilderScope.() -> Unit)

    /**
     * Background of the QR code.
     * Can be [DrawableSource], [QrVectorColor] or both
     * */
    fun background(block: QrVectorBackgroundBuilderScope.() -> Unit)

    /**
     * Middle image.
     * */
    fun logo(block: QrVectorLogoBuilderScope.() -> Unit)
}




