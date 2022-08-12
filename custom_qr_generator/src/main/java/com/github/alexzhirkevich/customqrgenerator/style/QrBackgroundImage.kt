package com.github.alexzhirkevich.customqrgenerator.style

import android.graphics.drawable.Drawable
import androidx.annotation.FloatRange

/**
 * Background image of the QR code bitmap.
 *
 * @property alpha can be applied to make background image less noticeable.
 * @property drawable logo image. Can have transparent background
 * @property scale way of getting necessary scaled bitmap from [drawable]
 * */
data class QrBackgroundImage(
    val drawable: Drawable,
    @FloatRange(from = 0.0, to = 1.0)
    val alpha : Float = 1f,
    val scale: BitmapScale = BitmapScale.FitCenter
)