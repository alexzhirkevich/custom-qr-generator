package com.github.alexzhirkevich.customqrgenerator.vector

import androidx.annotation.FloatRange
import com.github.alexzhirkevich.customqrgenerator.QrErrorCorrectionLevel
import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import com.github.alexzhirkevich.customqrgenerator.SerializersModuleFromProviders
import com.github.alexzhirkevich.customqrgenerator.style.QrOffset
import com.github.alexzhirkevich.customqrgenerator.style.QrOffsetBuilder
import com.github.alexzhirkevich.customqrgenerator.style.QrShape
import com.github.alexzhirkevich.customqrgenerator.vector.dsl.InternalQrVectorOptionsBuilderScope
import com.github.alexzhirkevich.customqrgenerator.vector.dsl.QrVectorOptionsBuilderScope
import com.github.alexzhirkevich.customqrgenerator.vector.style.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule

@Serializable
data class QrVectorOptions(
    @FloatRange(from = .0, to = .5)
    val padding : Float = .125f,
    val offset: QrOffset,
    val shapes: QrVectorShapes,
    val codeShape : QrShape,
    val colors : QrVectorColors,
    val logo : QrVectorLogo,
    val background: QrVectorBackground,
    val errorCorrectionLevel: QrErrorCorrectionLevel
)  {
    class Builder : QrOffsetBuilder, QrVectorLogoBuilder {

        @FloatRange(from = .0, to = .5)
        var padding : Float = 0f
        override var offset: QrOffset = QrOffset(0f,0f)
        var shapes: QrVectorShapes = QrVectorShapes()
        var shape : QrShape = QrShape.Default
        var colors : QrVectorColors = QrVectorColors()
        override var logo : QrVectorLogo = QrVectorLogo()
        var background: QrVectorBackground = QrVectorBackground()
        var errorCorrectionLevel: QrErrorCorrectionLevel = QrErrorCorrectionLevel.Low

        fun setPadding(@FloatRange(from = .0, to = .5) padding: Float) = apply {
            this.padding = padding
        }

        fun setOffset(offset: QrOffset) = apply {
            this.offset = offset
        }

        fun setShapes(shapes: QrVectorShapes) = apply {
            this.shapes = shapes
        }

        fun setColors(colors: QrVectorColors) = apply {
            this.colors = colors
        }

        fun setCodeShape(shape: QrShape) = apply{
            this.shape = shape
        }

        fun setLogo(logo: QrVectorLogo) = apply {
            this.logo = logo
        }

        fun setBackground(background: QrVectorBackground) = apply {
            this.background = background
        }

        fun setErrorCorrectionLevel(errorCorrectionLevel: QrErrorCorrectionLevel) = apply {
            this.errorCorrectionLevel = errorCorrectionLevel
        }

        fun build() : QrVectorOptions = QrVectorOptions(
            padding, offset, shapes, shape, colors, logo, background, errorCorrectionLevel
        )
    }

    companion object : SerializationProvider {
        @ExperimentalSerializationApi
        override val defaultSerializersModule: SerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModuleFromProviders(
                QrVectorShapes, QrVectorColors, QrVectorLogo, QrShape, QrVectorBackground
            )
        }
    }
}

fun createQrVectorOptions(block : QrVectorOptionsBuilderScope.() -> Unit) : QrVectorOptions {
    val builder = QrVectorOptions.Builder()
    InternalQrVectorOptionsBuilderScope(builder).apply(block)
    return builder.build()
}

