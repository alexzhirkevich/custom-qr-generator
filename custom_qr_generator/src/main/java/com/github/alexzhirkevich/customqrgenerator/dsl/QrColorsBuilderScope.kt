@file:Suppress("DEPRECATION")

package com.github.alexzhirkevich.customqrgenerator.dsl

import com.github.alexzhirkevich.customqrgenerator.QrOptions
import com.github.alexzhirkevich.customqrgenerator.style.IQRColors
import com.github.alexzhirkevich.customqrgenerator.style.QrColor


sealed interface QrColorsBuilderScope : IQRColors {
    override var light : QrColor
    override var dark : QrColor
    override var frame : QrColor
    override var ball : QrColor
    override var highlighting : QrColor
    override var symmetry : Boolean
}

class InternalColorsBuilderScope internal constructor(
    val builder: QrOptions.Builder
) : QrColorsBuilderScope {

    override var light: QrColor
        get() = builder.colors.light
        set(value)  = with(builder) {
            colors(colors.copy(light = value))
        }

    override var dark: QrColor
        get() = builder.colors.dark
        set(value) = with(builder) {
                colors(colors.copy(dark = value))
            }

    override var frame: QrColor
        get() = builder.colors.frame
        set(value) = with(builder) {
            colors(colors.copy(frame = value))
        }

    override var ball: QrColor
        get() = builder.colors.ball
        set(value) = with(builder) {
            colors(colors.copy(ball = value))
        }

    override var highlighting: QrColor
        get() = builder.colors.highlighting
        set(value) = with(builder) {
            colors(colors.copy(highlighting = value))
        }

    override var symmetry: Boolean
        get() = builder.colors.symmetry
        set(value) = with(builder) {
            colors(colors.copy(symmetry = value))
        }
}