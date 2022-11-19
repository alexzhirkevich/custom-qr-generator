package com.github.alexzhirkevich.customqrgenerator.vector.dsl

import com.github.alexzhirkevich.customqrgenerator.style.*
import com.github.alexzhirkevich.customqrgenerator.vector.style.*

/**
 * @see QrLogo
 * */
sealed interface QrVectorLogoBuilderScope : IQRVectorLogo {

    override var drawable: DrawableSource
    override var size : Float
    override var padding : QrVectorLogoPadding
    override var shape: QrVectorLogoShape
    override var scale: BitmapScale
    override var backgroundColor : QrVectorColor
}

internal class InternalQrVectorLogoBuilderScope(
    val builder: QrVectorLogoBuilder,
) : QrVectorLogoBuilderScope {

    override var drawable: DrawableSource
        get() = builder.logo.drawable
        set(value) = with(builder) {
            logo = logo.copy(drawable = value)
        }
    override var size: Float
        get() = builder.logo.size
        set(value) = with(builder) {
            logo = logo.copy(size = value)
        }

    override var padding: QrVectorLogoPadding
        get() = builder.logo.padding
        set(value) = with(builder) {
            logo =logo.copy(padding = value)
        }
    override var shape: QrVectorLogoShape
        get() = builder.logo.shape
        set(value) = with(builder) {
            logo = logo.copy(shape = value)
        }

    override var scale: BitmapScale
        get() = builder.logo.scale
        set(value) = with(builder) {
            logo = logo.copy(scale = value)
        }
    override var backgroundColor: QrVectorColor
        get() = builder.logo.backgroundColor
        set(value) = with(builder) {
            logo = logo.copy(backgroundColor = value)
        }
}