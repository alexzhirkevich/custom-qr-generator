package com.github.alexzhirkevich.customqrgenerator.vector.style

import androidx.annotation.FloatRange

interface QrVectorLogoShape : QrVectorShapeModifier {

    
    object Default : QrVectorLogoShape, QrVectorShapeModifier by DefaultVectorShape

    
    object Circle : QrVectorLogoShape, QrVectorShapeModifier by CircleVectorShape(1f)

    
    data class RoundCorners(
        @FloatRange(from = 0.0, to = .5) val radius: Float
    ) : QrVectorLogoShape, QrVectorShapeModifier by RoundCornersVectorShape(radius, false)

    
    object Rhombus : QrVectorLogoShape, QrVectorShapeModifier by RhombusVectorShape(1f)
}