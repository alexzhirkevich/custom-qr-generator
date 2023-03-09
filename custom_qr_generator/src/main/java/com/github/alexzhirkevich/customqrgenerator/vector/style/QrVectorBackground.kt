package com.github.alexzhirkevich.customqrgenerator.vector.style

import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import com.github.alexzhirkevich.customqrgenerator.SerializersModuleFromProviders
import com.github.alexzhirkevich.customqrgenerator.style.BitmapScale
import com.github.alexzhirkevich.customqrgenerator.style.DrawableSource
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule

interface IQrVectorBackground  {

    /**
     * Background image of QR code. Applied aon top of [color]
     * */
    val drawable: DrawableSource
    val scale: BitmapScale
    val color : QrVectorColor
}

@Serializable
data class QrVectorBackground(
    override val drawable: DrawableSource = DrawableSource.Empty,
    override val scale: BitmapScale = BitmapScale.FitXY,
    override val color : QrVectorColor = QrVectorColor.Transparent
) : IQrVectorBackground{

    companion object : SerializationProvider {
        @ExperimentalSerializationApi
        override val defaultSerializersModule: SerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModuleFromProviders(
                DrawableSource, BitmapScale, QrVectorColor
            )
        }
    }
}