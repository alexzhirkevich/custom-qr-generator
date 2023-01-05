package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange
import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import com.github.alexzhirkevich.customqrgenerator.SerializersModuleFromProviders
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 *
 *
 * @property drawable logo image. Can have transparent background
 * @property alpha can be applied to make background image less noticeable.
 * @property scale way of getting necessary scaled bitmap from [drawable]
 * @property color color of the QR code background. Applied behind image
 * */
interface IQRBackground {
    val drawable: DrawableSource
    val alpha : Float
    val scale: BitmapScale
    val color: QrColor
}

/**
 * Background image of the QR code bitmap.
 */
@Serializable
@Deprecated("Use QrCodeDrawable with QrVectorBackground instead")
data class QrBackground(
    override val drawable: DrawableSource = DrawableSource.Empty,
    @FloatRange(from = 0.0, to = 1.0)
    override val alpha : Float = 1f,
    override val scale: BitmapScale = BitmapScale.FitXY,
    override val color: QrColor = QrColor.Solid(Color(0xffffffff))
) : IQRBackground {
    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        override val defaultSerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModuleFromProviders(
                DrawableSource,
                BitmapScale,
                QrColor
            )
        }
    }
}