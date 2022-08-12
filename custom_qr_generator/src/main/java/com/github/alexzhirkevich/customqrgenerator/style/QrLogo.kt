package com.github.alexzhirkevich.customqrgenerator.style

import android.graphics.drawable.Drawable
import androidx.annotation.FloatRange

/**
 * @property drawable logo image. Can have transparent background
 * @property size logo size relative to qr-code size.
 * If logo size is bigger than allowed size, QR code might be unreadable.
 * @property padding logo padding relative to the logo size
 * @property shape shape of logo image and padding.
 * @property scale way of getting necessary scaled bitmap from [drawable]
 * */
data class QrLogo(
    val drawable: Drawable,
    @FloatRange(from = 0.0, to = .33)
    val size : Float = 0.2f,
    val padding : QrLogoPadding = QrLogoPadding.Empty,
    val shape: QrLogoShape = QrLogoShape.Default,
    val scale: BitmapScale = BitmapScale.FitCenter,
)

