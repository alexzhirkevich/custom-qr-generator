package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.Path
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors

interface QrVectorShapeModifier {

    /**
     * If shape is not depend on neighbors, it will be drawn faster.
     * Only [QrVectorPixelShape] can be depended on neighbors.
     * */
    val isDependOnNeighbors : Boolean

    fun createPath(size : Float, neighbors: Neighbors) : Path
}