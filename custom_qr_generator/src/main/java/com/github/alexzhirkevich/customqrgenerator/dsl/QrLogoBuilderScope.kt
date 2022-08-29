package com.github.alexzhirkevich.customqrgenerator.dsl

import com.github.alexzhirkevich.customqrgenerator.QrOptions
import com.github.alexzhirkevich.customqrgenerator.style.*


/**
 * @see QrLogo
 * */
sealed interface QrLogoBuilderScope : IQRLogo {

    override var drawable: DrawableSource
    override var size : Float
    override var padding : QrLogoPadding
    override var shape: QrLogoShape
    override var scale: BitmapScale
    override var backgroundColor : QrColor
}



internal class InternalQrLogoBuilderScope(
     val builder: QrOptions.Builder
) : QrLogoBuilderScope {

    override var drawable: DrawableSource
        get() = builder.logo.drawable
        set(value) = with(builder) {
            setLogo(logo.copy(drawable = value))
        }
    override var size: Float
        get() = builder.logo.size
        set(value) = with(builder) {
            setLogo(logo.copy(size = value))
        }

    override var padding: QrLogoPadding
        get() = builder.logo.padding
        set(value) = with(builder) {
            setLogo(logo.copy(padding = value))
        }
    override var shape: QrLogoShape
        get() = builder.logo.shape
        set(value) = with(builder) {
            setLogo(logo.copy(shape = value))
        }

    override var scale: BitmapScale
        get() = builder.logo.scale
        set(value) = with(builder) {
            setLogo(logo.copy(scale = value))
        }
    override var backgroundColor: QrColor
        get() = builder.logo.backgroundColor
        set(value) = with(builder) {
            setLogo(logo.copy(backgroundColor = value))
        }
}