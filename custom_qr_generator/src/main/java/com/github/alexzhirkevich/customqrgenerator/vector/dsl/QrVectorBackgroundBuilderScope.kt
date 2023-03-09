package com.github.alexzhirkevich.customqrgenerator.vector.dsl

import com.github.alexzhirkevich.customqrgenerator.style.BitmapScale
import com.github.alexzhirkevich.customqrgenerator.style.DrawableSource
import com.github.alexzhirkevich.customqrgenerator.vector.style.IQrVectorBackground
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor

sealed interface QrVectorBackgroundBuilderScope : IQrVectorBackground {

    override var drawable: DrawableSource
    override var scale: BitmapScale
    override var color: QrVectorColor
}