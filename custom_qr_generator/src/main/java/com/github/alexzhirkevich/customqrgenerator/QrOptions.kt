package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Canvas
import android.graphics.Paint
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
    class Builder(@IntRange(from = 0) val size : Int){

        var padding = .125f
        var colors = QrColors()
        var logo: QrLogo? = null
        var backgroundImage: QrBackgroundImage? = null
        var elementsShapes = QrElementsShapes()
        var codeShape : QrShape = QrShape.Default
        var errorCorrectionLevel : QrErrorCorrectionLevel = QrErrorCorrectionLevel.Auto

        fun build() : QrOptions = QrOptions(
            size, padding,colors, logo, backgroundImage,elementsShapes, codeShape, errorCorrectionLevel
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
            this.backgroundImage = background
        }

        fun setCodeShape(shape: QrShape) : Builder = apply{
            this.codeShape =shape
        }

        fun setElementsShapes(shapes: QrElementsShapes) = apply {
            this.elementsShapes = shapes
        }

        fun setErrorCorrectionLevel(level: QrErrorCorrectionLevel) = apply{
            errorCorrectionLevel = level
        }
    }
}

/**
 * Build [QrOptions] via Kotlin DSL
 * */
fun createQrOptions(size: Int, build :QrOptions.Builder.() -> Unit) : QrOptions {
    return QrOptions.Builder(size).apply(build).build()
}

/**
 * Create custom [QrShapeModifier] by drawing on [Canvas].
 * Can be converted to a specific shape modifier with corresponding
 * as___Shape function
 * @see QrCanvasShapeModifier
 * @see QrShapeModifier.asPixelShape
 * @see QrShapeModifier.asBallShape
 * @see QrShapeModifier.asFrameShape
 * @see QrShapeModifier.asLogoShape
 * @see QrShapeModifier.asBackgroundShape
 * */
fun QrOptions.Builder.drawShape(draw : (canvas : Canvas, drawPaint : Paint, erasePaint : Paint) -> Unit)
        : QrShapeModifier = object : QrCanvasShapeModifier {

    override fun draw(canvas: Canvas, drawPaint: Paint, erasePaint: Paint) =
        draw(canvas, drawPaint, erasePaint)

}.toShapeModifier(size)