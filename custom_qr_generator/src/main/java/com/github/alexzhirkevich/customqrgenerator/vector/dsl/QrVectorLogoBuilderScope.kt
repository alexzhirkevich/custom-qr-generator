package com.github.alexzhirkevich.customqrgenerator.vector.dsl

import android.graphics.drawable.Drawable
import com.github.alexzhirkevich.customqrgenerator.style.*
import com.github.alexzhirkevich.customqrgenerator.vector.style.*

sealed interface QrVectorLogoBuilderScope : IQRVectorLogo {

    override var drawable: Drawable?
    override var size : Float
    override var padding : QrVectorLogoPadding
    override var shape: QrVectorLogoShape
    override var scale: BitmapScale
    override var backgroundColor : QrVectorColor
}

