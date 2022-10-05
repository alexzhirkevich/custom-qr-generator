package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange
import kotlinx.serialization.Serializable

/**
 * @property x horizontal offset of the QR code pattern relative to it padding
 * @property y vertical offset of the QR code pattern relative to it padding
 * */
interface IQrOffset {
    val x : Float
    val y : Float
}

/**
 * Offset of the QR code pattern relative to QR code padding
 * */
@Serializable
data class QrOffset(
    @FloatRange(from = -1.0, to = 1.0) override val x : Float,
    @FloatRange(from = -1.0, to = 1.0) override val y : Float,
) : IQrOffset {
    companion object {
        val Zero = QrOffset(0f,0f)
    }
}

interface QrOffsetBuilder {
    var offset : QrOffset
}