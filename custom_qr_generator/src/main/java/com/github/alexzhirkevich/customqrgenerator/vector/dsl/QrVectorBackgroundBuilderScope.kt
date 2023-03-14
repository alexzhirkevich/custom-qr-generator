package com.github.alexzhirkevich.customqrgenerator.vector.dsl

import android.graphics.drawable.Drawable
import com.github.alexzhirkevich.customqrgenerator.style.BitmapScale
import com.github.alexzhirkevich.customqrgenerator.vector.style.IQrVectorBackground
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor

sealed interface QrVectorBackgroundBuilderScope : IQrVectorBackground {

    override var drawable: Drawable?
    override var scale: BitmapScale
    override var color: QrVectorColor
}