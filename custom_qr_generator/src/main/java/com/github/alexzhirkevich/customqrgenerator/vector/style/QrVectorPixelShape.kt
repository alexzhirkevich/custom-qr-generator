package com.github.alexzhirkevich.customqrgenerator.vector.style

import androidx.annotation.FloatRange

/**
 * Style of the qr-code pixels.
 * */
interface QrVectorPixelShape : QrVectorShapeModifier {

    
    object Default : QrVectorPixelShape, QrVectorShapeModifier by DefaultVectorShape

    
    data class Circle(
        @FloatRange(from = 0.0, to = 1.0) val size: Float = 1f
    ) : QrVectorPixelShape, QrVectorShapeModifier by CircleVectorShape(size)

    
    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val radius : Float
    ) : QrVectorPixelShape, QrVectorShapeModifier by RoundCornersVectorShape(radius,true)

    
    data class Rhombus(
        @FloatRange(from = 0.0, to = 1.0) private val scale : Float = 1f
    ): QrVectorPixelShape, QrVectorShapeModifier by RhombusVectorShape(scale)

    
    object Star : QrVectorPixelShape, QrVectorShapeModifier by StarVectorShape


    data class RoundCornersVertical(
        @FloatRange(from = 0.0, to = 1.0) private val width : Float = 1f
    ): QrVectorPixelShape, QrVectorShapeModifier by RoundCornersVerticalVectorShape(width)

    
    data class RoundCornersHorizontal(
        @FloatRange(from = 0.0, to = 1.0) private val width : Float = 1f
    ): QrVectorPixelShape, QrVectorShapeModifier by RoundCornersHorizontalVectorShape(width)

}