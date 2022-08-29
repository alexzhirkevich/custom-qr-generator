package com.github.alexzhirkevich.customqrgenerator.style

import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import com.github.alexzhirkevich.customqrgenerator.SerializersModuleFromProviders
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * @property darkPixel shape of dark pixels
 * @property lightPixel shape of light pixels
 * @property frame shape of qr-code eye frames
 * @property ball shape of qr-code eye balls
 * @property highlighting shape of qr-code background without side paddings.
 * [QrColors.highlighting] will be applied according to this shape
 * */
interface IQRElementsShapes {
    val darkPixel : QrPixelShape
    val lightPixel : QrPixelShape
    val frame : QrFrameShape
    val ball : QrBallShape
    val highlighting : QrHighlightingShape
}

/**
 * Shapes of QR code elements
 * */
@Serializable
data class QrElementsShapes(
    override val darkPixel : QrPixelShape = QrPixelShape.Default,
    override val lightPixel : QrPixelShape = QrPixelShape.Default,
    override val frame : QrFrameShape = QrFrameShape.Default,
    override val ball : QrBallShape = QrBallShape.Default,
    override val highlighting : QrHighlightingShape = QrHighlightingShape.Default
) : IQRElementsShapes {
    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        override val defaultSerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModuleFromProviders(
                QrPixelShape,
                QrFrameShape,
                QrBallShape,
                QrHighlightingShape
            )
        }
    }
}