package com.github.alexzhirkevich.customqrgenerator.dsl

import com.github.alexzhirkevich.customqrgenerator.QrOptions
import com.github.alexzhirkevich.customqrgenerator.style.*


/**
 * @see QrElementsShapes
 * */
sealed interface QrElementsShapesBuilderScope : IQRElementsShapes {
    override var darkPixel : QrPixelShape
    override var lightPixel : QrPixelShape
    override var frame : QrFrameShape
    override var ball : QrBallShape
    override var highlighting : QrHighlightingShape
}



internal class InternalQrElementsShapesBuilderScope(
    val builder: QrOptions.Builder
) : QrElementsShapesBuilderScope {
    override var darkPixel: QrPixelShape
        get() = builder.elementsShapes.darkPixel
        set(value) = with(builder) {
            shapes(elementsShapes.copy(darkPixel = value))

        }
    override var lightPixel: QrPixelShape
        get() = builder.elementsShapes.lightPixel
        set(value) = with(builder) {
            shapes(elementsShapes.copy(lightPixel = value))
        }

    override var frame: QrFrameShape
        get() = builder.elementsShapes.frame
        set(value) = with(builder){
                shapes(elementsShapes.copy(frame = value))
            }

    override var ball: QrBallShape
        get() = builder.elementsShapes.ball
        set(value) = with(builder){
            shapes(elementsShapes.copy(ball = value))
        }
    override var highlighting: QrHighlightingShape
        get() = builder.elementsShapes.highlighting
        set(value) = with(builder){
            shapes(elementsShapes.copy(highlighting = value))
        }
}