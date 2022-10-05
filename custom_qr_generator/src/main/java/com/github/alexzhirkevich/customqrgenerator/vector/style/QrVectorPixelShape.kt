package com.github.alexzhirkevich.customqrgenerator.vector.style

import androidx.annotation.FloatRange

interface QrVectorPixelShape : QrVectorShapeModifier {

    object Default : QrVectorPixelShape, QrVectorShapeModifier by DefaultVectorShape

    data class Circle(
        @FloatRange(from = 0.0, to = 1.0) val size: Float
    ) : QrVectorPixelShape, QrVectorShapeModifier by CircleVectorShape(size)

    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val radius : Float
    ) : QrVectorPixelShape, QrVectorShapeModifier by RoundCornersVectorShape(radius)  {

        override val isDependOnNeighbors: Boolean get() = true
    }
}