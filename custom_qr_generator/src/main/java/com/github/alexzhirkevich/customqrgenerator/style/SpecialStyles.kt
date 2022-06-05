package com.github.alexzhirkevich.customqrgenerator.style

internal sealed interface AsPixels : QrModifier {
    val pixelStyle : QrPixelStyle
}