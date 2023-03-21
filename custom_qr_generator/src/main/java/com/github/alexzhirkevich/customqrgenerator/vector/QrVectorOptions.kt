package com.github.alexzhirkevich.customqrgenerator.vector

import androidx.annotation.FloatRange
import com.github.alexzhirkevich.customqrgenerator.QrErrorCorrectionLevel
import com.github.alexzhirkevich.customqrgenerator.style.QrOffset
import com.github.alexzhirkevich.customqrgenerator.style.QrShape
import com.github.alexzhirkevich.customqrgenerator.vector.dsl.InternalQrVectorOptionsBuilderScope
import com.github.alexzhirkevich.customqrgenerator.vector.dsl.QrVectorOptionsBuilderScope
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBackground
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColors
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogo
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorShapes


data class QrVectorOptions(
    @FloatRange(from = .0, to = .5)
    val padding : Float = .125f,
    val offset: QrOffset,
    val shapes: QrVectorShapes,
    val codeShape : QrShape,
    val colors : QrVectorColors,
    val logo : QrVectorLogo,
    val background: QrVectorBackground,
    val errorCorrectionLevel: QrErrorCorrectionLevel,
    val fourthEyeEnabled : Boolean
) {
    class Builder {

        @FloatRange(from = .0, to = .5)
        var padding: Float = 0f
            private set
        var offset: QrOffset = QrOffset(0f, 0f)
            private set
        var shapes: QrVectorShapes = QrVectorShapes()
            private set
        var shape: QrShape = QrShape.Default
            private set
        var colors: QrVectorColors = QrVectorColors()
            private set
        var logo: QrVectorLogo = QrVectorLogo()
            private set
        var background: QrVectorBackground = QrVectorBackground()
            private set
        var errorCorrectionLevel: QrErrorCorrectionLevel = QrErrorCorrectionLevel.Auto
            private set
        var fourthEyeEnabled: Boolean = false
            private set

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

        fun setCodeShape(shape: QrShape) = apply {
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

        fun setFourthEyeEnabled(enabled: Boolean) = apply {
            this.fourthEyeEnabled = enabled
        }

        fun build(): QrVectorOptions = QrVectorOptions(
            padding = padding,
            offset = offset,
            shapes = shapes,
            codeShape = shape,
            colors = colors,
            logo = logo,
            background = background,
            errorCorrectionLevel = errorCorrectionLevel,
            fourthEyeEnabled = fourthEyeEnabled
        )
    }
}
fun createQrVectorOptions(block : QrVectorOptionsBuilderScope.() -> Unit) : QrVectorOptions {
    val builder = QrVectorOptions.Builder()
    InternalQrVectorOptionsBuilderScope(builder).apply(block)
    return builder.build()
}

