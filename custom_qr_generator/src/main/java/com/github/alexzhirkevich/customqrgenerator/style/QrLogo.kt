package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange
import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import com.github.alexzhirkevich.customqrgenerator.SerializersModuleFromProviders
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * @property drawable logo image
 * @property size logo size relative to qr-code size. Should be from 0 to 1/3.
 * If logo size is bigger than allowed size, QR code might be unreadable.
 * @property padding logo padding relative to the logo [size]
 * @property shape shape of logo image and [padding].
 * @property scale way of getting necessary scaled bitmap from [drawable]
 * @property backgroundColor color of the logo background.
 * Does not applied to [padding].
 * Transparency works through QR code background.
 * [QrColor.Unspecified] means that logo background will be painted
 * as QR code background
 * */

interface IQRLogo {
    val drawable: DrawableSource
    val size: Float
    val padding : QrLogoPadding
    val shape: QrLogoShape
    val scale: BitmapScale
    val backgroundColor : QrColor
}

/**
 * Logo of the QR code
 * */
@Serializable
@Deprecated("Use QrCodeDrawable with QrVectorLogo instead")
data class QrLogo(
    override val drawable: DrawableSource = DrawableSource.Empty,
    @FloatRange(from = 0.0, to = 1/3.0)
    override val size : Float = 0.2f,
    override val padding : QrLogoPadding = QrLogoPadding.Empty,
    override val shape: QrLogoShape = QrLogoShape.Default,
    override val scale: BitmapScale = BitmapScale.FitXY,
    override val backgroundColor : QrColor = QrColor.Unspecified
) : IQRLogo {

    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        override val defaultSerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModuleFromProviders(
                DrawableSource,
                QrLogoPadding,
                QrLogoShape,
                BitmapScale,
                QrColor
            )
        }
    }
}

interface QrLogoBuilder {
    var logo : QrLogo
}

