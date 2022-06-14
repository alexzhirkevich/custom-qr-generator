package com.github.alexzhirkevich.customqrgenerator.style

import android.graphics.drawable.Drawable

/**
 * Background image of the QR code bitmap.
 * Alpha can be applied to make background image less noticeable.
 * */
data class QrBackgroundImage(
    val drawable: Drawable,
    val alpha : Float = 1f
)