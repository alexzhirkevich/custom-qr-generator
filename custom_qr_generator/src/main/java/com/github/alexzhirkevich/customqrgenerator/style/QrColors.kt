@file:Suppress("deprecation")

package com.github.alexzhirkevich.customqrgenerator.style


/**
 *
 * @property light color of light QR code dots
 * @property dark color of dark QR code dots
 * @property frame color of code eyes frame
 * @property ball color of code eyes ball
 * @property highlighting color of code background (above image, after paddings)
 * Shape regulated by [QrElementsShapes.highlighting]
 * @property symmetry if false, all eyes will be the same color. Otherwise,
 * color will be flipped according to eye position
 * */
interface IQRColors {
    val light : QrColor
    val dark : QrColor
    val frame : QrColor
    val ball : QrColor
    val highlighting : QrColor
    val symmetry : Boolean
}

/**
 * Colors of QR code elements
 */

@Deprecated("Use QrCodeDrawable with QrVectorColors instead")
data class QrColors(
    override val light : QrColor = QrColor.Unspecified,
    override val dark : QrColor = QrColor.Solid(Color(0xff000000)),
    override val frame : QrColor = QrColor.Unspecified,
    override val ball : QrColor = QrColor.Unspecified,
    override val highlighting : QrColor = QrColor.Unspecified,
    override val symmetry : Boolean = true,
) : IQRColors