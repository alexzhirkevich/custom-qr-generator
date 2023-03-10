package com.github.alexzhirkevich.customqrgenerator.vector.dsl

import com.github.alexzhirkevich.customqrgenerator.style.BitmapScale
import com.github.alexzhirkevich.customqrgenerator.style.DrawableSource
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor

internal class InternalQrVectorBackgroundBuilderScope(
    val builder: QrVectorOptions.Builder
) : QrVectorBackgroundBuilderScope {

    override var drawable: DrawableSource
        get() = builder.background.drawable
        set(value) = with(builder){
            setBackground(background.copy(drawable = value))
        }
    override var scale: BitmapScale
        get() = builder.background.scale
        set(value) = with(builder){
            setBackground(background.copy(scale = value))
        }
    override var color: QrVectorColor
        get() = builder.background.color
        set(value) = with(builder) {
            setBackground(background.copy(color = value))
        }
}