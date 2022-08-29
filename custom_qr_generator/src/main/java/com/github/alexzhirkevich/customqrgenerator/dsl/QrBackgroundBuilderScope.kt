package com.github.alexzhirkevich.customqrgenerator.dsl

import com.github.alexzhirkevich.customqrgenerator.QrOptions
import com.github.alexzhirkevich.customqrgenerator.style.*

/**
 * @see QrBackground
 * */
sealed interface QrBackgroundBuilderScope : IQRBackground {

    override var drawable: DrawableSource

    override var alpha : Float

    override var scale: BitmapScale

    override var color : QrColor
}

internal class InternalQrBackgroundBuilderScope(
    val builder: QrOptions.Builder
) : QrBackgroundBuilderScope{

    override var drawable: DrawableSource
        get() = builder.background.drawable
        set(value) = with(builder) {
            setBackground(background.copy(drawable = value))
        }

    override var alpha: Float
        get() = builder.background.alpha
        set(value) = with(builder) {
            setBackground(background.copy(alpha = value))
        }

    override var scale: BitmapScale
        get() = builder.background.scale
        set(value) = with(builder) {
            setBackground(background.copy(scale = value))
        }

    override var color: QrColor
        get() = builder.background.color
        set(value) = with(builder) {
            setBackground(background.copy(color = value))
        }
}