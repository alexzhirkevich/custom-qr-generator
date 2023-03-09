package com.github.alexzhirkevich.customqrgenerator.vector.dsl

import com.github.alexzhirkevich.customqrgenerator.vector.style.IQrVectorColors
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor


sealed interface QrVectorColorsBuilderScope : IQrVectorColors {
    override var ball: QrVectorColor
    override var dark: QrVectorColor
    override var frame: QrVectorColor
    override var light: QrVectorColor
}

