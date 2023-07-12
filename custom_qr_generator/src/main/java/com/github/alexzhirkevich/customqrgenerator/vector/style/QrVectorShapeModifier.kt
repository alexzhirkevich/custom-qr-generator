package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.Path
import androidx.core.graphics.minus
import androidx.core.graphics.plus
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors

fun interface QrVectorShapeModifier {

    /**
     * Apply shape to a path
     *
     * @return The same instance if possible
     * */
    fun Path.shape(size : Float, neighbors: Neighbors)

}

