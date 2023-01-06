package com.github.alexzhirkevich.customqrgenerator.vector.style

import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import com.github.alexzhirkevich.customqrgenerator.SerializersModuleFromProviders
import com.github.alexzhirkevich.customqrgenerator.style.QrFrameShape
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule

interface IQrVectorShapes{
    val darkPixel: QrVectorPixelShape
    val lightPixel : QrVectorPixelShape
    val ball : QrVectorBallShape
    val frame : QrVectorFrameShape
    val centralSymmetry : Boolean
}

/**
 * Shapes of QR code elements
 * */
@Serializable
data class QrVectorShapes(
    override val darkPixel: QrVectorPixelShape = QrVectorPixelShape.Default,
    override val lightPixel : QrVectorPixelShape = QrVectorPixelShape.Default,
    override val ball : QrVectorBallShape = QrVectorBallShape.Default,
    override val frame : QrVectorFrameShape = QrVectorFrameShape.Default,
    override val centralSymmetry: Boolean = true
) : IQrVectorShapes{
    companion object : SerializationProvider {
        @ExperimentalSerializationApi
        override val defaultSerializersModule: SerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModuleFromProviders(
                QrVectorPixelShape,
                QrVectorBallShape,
                QrVectorFrameShape
            )
        }
    }
}