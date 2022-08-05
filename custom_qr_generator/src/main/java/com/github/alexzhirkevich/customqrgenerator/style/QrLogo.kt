package com.github.alexzhirkevich.customqrgenerator.style

import android.graphics.drawable.Drawable
import androidx.annotation.FloatRange

/**
 * @param drawable logo image. Can have transparent background
 * @param size logo size relative to qr-code size.
 * If logo size is bigger than allowed size, QR code might be unreadable.
 * @param padding logo padding  relative to logo size
 * @param shape shape of logo image and padding.
 * @param scale way of getting necessary scaled bitmap from [drawable]
 * You can implement custom [QrLogoShape]
 * */
data class QrLogo(
    val drawable: Drawable,
    @FloatRange(from = 0.0, to = .3)
    val size : Float = 0.2f,
    val padding : Float = 0.15f,
    val shape: QrLogoShape = QrLogoShape.Default,
    val scale: BitmapScale = BitmapScale.FitCenter,
)