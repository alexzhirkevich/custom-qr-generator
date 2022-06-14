package com.github.alexzhirkevich.customqrgenerator

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.github.alexzhirkevich.customqrgenerator.style.*

data class QrOptions(
    @IntRange(from = 0) val size : Int,
    @FloatRange(from = .0, to = .5) val padding : Float,
    val colors : QrColors,
    val logo: QrLogo?,
    val background: QrBackgroundImage?,
    val shapes: QrElementsShapes,
    val codeShape : QrShape,
    val errorCorrectionLevel: QrErrorCorrectionLevel
){
    class Builder(@IntRange(from = 0) private val size : Int){

        private var padding = .125f
        private var colors = QrColors()
        private var logo: QrLogo? = null
        private var background: QrBackgroundImage? = null
        private var shapes = QrElementsShapes()
        private var codeShape : QrShape = QrShape.Default
        private var errorCorrectionLevel : QrErrorCorrectionLevel = QrErrorCorrectionLevel.Auto

        fun build() : QrOptions = QrOptions(
            size, padding,colors, logo, background,shapes, codeShape, errorCorrectionLevel
        )

        /**
         * Padding of the QR code relative to [size].
         * */
        fun setPadding(@FloatRange(from = 0.0, to = .5) padding: Float) = apply {
            this.padding = padding
        }

        fun setColors(colors: QrColors) = apply{
            this.colors = colors
        }

        fun setLogo(logo : QrLogo?) = apply {
            this.logo = logo

        }

        fun setBackground(background: QrBackgroundImage?)  = apply{
            this.background = background
        }

        fun setCodeShape(shape: QrShape) : Builder = apply{
            codeShape =shape
        }

        fun setElementsShapes(shapes: QrElementsShapes) = apply {
            this.shapes = shapes
        }

        fun setErrorCorrectionLevel(level: QrErrorCorrectionLevel) = apply{
            errorCorrectionLevel = level
        }
    }
}