package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.drawable.Drawable
import androidx.annotation.FloatRange
import com.github.alexzhirkevich.customqrgenerator.style.BitmapScale

interface IQRVectorLogo {
    val drawable: Drawable?
    val size: Float
    val padding : QrVectorLogoPadding
    val shape: QrVectorLogoShape
    val scale: BitmapScale
    val backgroundColor : QrVectorColor
}

/**
 * Logo of the QR code
 * */

data class QrVectorLogo(
    override val drawable: Drawable? = null,
    @FloatRange(from = 0.0, to = 1/3.0)
    override val size: Float = 0.2f,
    override val padding: QrVectorLogoPadding = QrVectorLogoPadding.Empty,
    override val shape: QrVectorLogoShape = QrVectorLogoShape.Default,
    override val scale: BitmapScale = BitmapScale.FitXY,
    override val backgroundColor: QrVectorColor = QrVectorColor.Unspecified
) : IQRVectorLogo {

    class Builder : IQRVectorLogo {

        override var drawable: Drawable? = null
        @FloatRange(from = 0.0, to = 1/3.0)
        override var size: Float = 0.2f
        override var padding: QrVectorLogoPadding = QrVectorLogoPadding.Empty
        override var shape: QrVectorLogoShape = QrVectorLogoShape.Default
        override var scale: BitmapScale = BitmapScale.FitXY
        override var backgroundColor: QrVectorColor = QrVectorColor.Unspecified

        fun drawable(drawable: Drawable?) = apply {
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

}

