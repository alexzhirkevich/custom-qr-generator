package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.Path
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors

interface QrVectorShapeModifier {

    /**
     * Apply shape to a path
     *
     * @return The same instance if possible
     * */
    fun Path.shape(size : Float, neighbors: Neighbors) : Path

    @Deprecated("Replace with Path.shape(size,neighbors). Will be removed in 2.0",
        ReplaceWith("Path().apply { shape(size, neighbors) }", "android.graphics.Path")
    )
    fun createPath(size : Float, neighbors: Neighbors) : Path = Path().apply {
        shape(size, neighbors)
    }
}