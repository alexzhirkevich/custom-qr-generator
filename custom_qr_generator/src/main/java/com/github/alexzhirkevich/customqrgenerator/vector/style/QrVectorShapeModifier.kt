package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.Path
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors

fun interface QrVectorShapeModifier {

    fun createPath(size : Float, neighbors: Neighbors) : Path
}