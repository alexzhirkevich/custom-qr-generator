package com.github.alexzhirkevich.customqrgenerator.vector.style

import androidx.annotation.FloatRange
import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import com.github.alexzhirkevich.customqrgenerator.SerializersModuleFromProviders
import com.github.alexzhirkevich.customqrgenerator.style.*
import com.github.alexzhirkevich.customqrgenerator.style.IQRLogo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule

interface IQRVectorLogo {
    val drawable: DrawableSource
    val size: Float
    val padding : QrVectorLogoPadding
    val shape: QrVectorLogoShape
    val scale: BitmapScale
    val backgroundColor : QrVectorColor
}

/**
 * Logo of the QR code
 * */
@Serializable
data class QrVectorLogo(
    override val drawable: DrawableSource = DrawableSource.Empty,
    @FloatRange(from = 0.0, to = 1/3.0)
    override val size: Float = 0.2f,
    override val padding: QrVectorLogoPadding = QrVectorLogoPadding.Empty,
    override val shape: QrVectorLogoShape = QrVectorLogoShape.Default,
    override val scale: BitmapScale = BitmapScale.FitXY,
    override val backgroundColor: QrVectorColor = QrVectorColor.Unspecified
) : IQRVectorLogo {

    class Builder : IQRVectorLogo {

        override var drawable: DrawableSource = DrawableSource.Empty
        @FloatRange(from = 0.0, to = 1/3.0)
        override var size: Float = 0.2f
        override var padding: QrVectorLogoPadding = QrVectorLogoPadding.Empty
        override var shape: QrVectorLogoShape = QrVectorLogoShape.Default
        override var scale: BitmapScale = BitmapScale.FitXY
        override var backgroundColor: QrVectorColor = QrVectorColor.Unspecified

        fun drawable(drawable: DrawableSource) = apply {
            this.drawable = drawable
        }

        fun size(size: Float) = apply {
            this.size = size
        }

        fun padding(padding: QrVectorLogoPadding) = apply {
            this.padding = padding
        }

        fun shape(shape: QrVectorLogoShape) = apply {
            this.shape = shape
        }

        fun scale(scale: BitmapScale) = apply {
            this.scale = scale
        }

        fun backgroundColor(color: QrVectorColor) = apply {
            this.backgroundColor = color
        }

        fun build() : QrVectorLogo = QrVectorLogo(
            drawable = drawable,
            size = size,
            padding = padding,
            shape = shape,
            scale = scale,
            backgroundColor = backgroundColor
        )
    }

    companion object : SerializationProvider {
        @ExperimentalSerializationApi
        override val defaultSerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModuleFromProviders(QrLogoPadding, QrLogoShape, QrVectorColor)
        }
    }
}


interface QrVectorLogoBuilder {
    var logo : QrVectorLogo
}