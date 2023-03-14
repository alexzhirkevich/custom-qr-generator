@file:Suppress("deprecation")

package com.github.alexzhirkevich.customqrgenerator.style

import android.graphics.drawable.Drawable
import androidx.annotation.FloatRange

/**
 *
 *
 * @property drawable logo image. Can have transparent background
 * @property alpha can be applied to make background image less noticeable.
 * @property scale way of getting necessary scaled bitmap from [drawable]
 * @property color color of the QR code background. Applied behind image
 * */
interface IQRBackground {
    val drawable: Drawable?
    val alpha : Float
    val scale: BitmapScale
    val color: QrColor
}

/**
 * Background image of the QR code bitmap.
 */

@Deprecated("Use QrCodeDrawable with QrVectorBackground instead")
data class QrBackground(
    override val drawable: Drawable? = null,
    @FloatRange(from = 0.0, to = 1.0)
    override val alpha : Float = 1f,
    override val scale: BitmapScale = BitmapScale.FitXY,
    override val color: QrColor = QrColor.Solid(Color(0xffffffff))
) : IQRBackground
