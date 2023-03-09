package com.github.alexzhirkevich.customqrgenerator.vector.dsl

import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor

internal class InternalQrVectorColorsBuilderScope(
    private val builder: QrVectorOptions.Builder
) :  QrVectorColorsBuilderScope {
    override var dark: QrVectorColor
        get() = builder.colors.dark
        set(value) = with(builder){
            colors(colors.copy(
                dark = value
            ))
        }

    override var light: QrVectorColor
        get() = builder.colors.light
        set(value) = with(builder){
            colors(colors.copy(
                light = value
            ))
        }

    override var ball: QrVectorColor
        get() = builder.colors.ball
        set(value) = with(builder){
            colors(colors.copy(
                ball = value
            ))
        }
    override var frame: QrVectorColor
        get() = builder.colors.frame
        set(value) = with(builder){
            colors(colors.copy(
                frame = value
            ))
        }
}