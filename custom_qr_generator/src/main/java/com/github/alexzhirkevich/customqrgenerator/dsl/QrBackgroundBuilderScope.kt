@file:Suppress("DEPRECATION")

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

class InternalQrBackgroundBuilderScope internal constructor(
    val builder: QrOptions.Builder
) : QrBackgroundBuilderScope{

    override var drawable: DrawableSource
        get() = builder.background.drawable
        set(value) = with(builder) {
            background(background.copy(drawable = value))
        }

    override var alpha: Float
        get() = builder.background.alpha
        set(value) = with(builder) {
            background(background.copy(alpha = value))
        }

    override var scale: BitmapScale
        get() = builder.background.scale
        set(value) = with(builder) {
            background(background.copy(scale = value))
        }

    override var color: QrColor
        get() = builder.background.color
        set(value) = with(builder) {
            background(background.copy(color = value))
        }
}