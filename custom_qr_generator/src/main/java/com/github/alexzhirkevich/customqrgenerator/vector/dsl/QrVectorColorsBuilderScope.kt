package com.github.alexzhirkevich.customqrgenerator.vector.dsl

import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.IQrVectorColors
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrBlendMode
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor

sealed interface QrVectorColorsBuilderScope : IQrVectorColors {
    override var ball: QrVectorColor
    override var dark: QrVectorColor
    override var frame: QrVectorColor
    override var light: QrVectorColor
}

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