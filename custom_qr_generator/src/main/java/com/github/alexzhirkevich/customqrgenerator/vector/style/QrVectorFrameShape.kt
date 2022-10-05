package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.Paint
import android.graphics.Path
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors

interface QrVectorFrameShape : QrVectorShapeModifier {

    override val isDependOnNeighbors: Boolean get() = false

    object Default : QrVectorFrameShape {

        override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {
            val width = size/7f
            addRect(0f,0f,size,width,Path.Direction.CW)
            addRect(0f,0f,width,size,Path.Direction.CW)
            addRect(size-width,0f,size,size,Path.Direction.CW)
            addRect(0f,size-width,size,size,Path.Direction.CW)
        }
    }
}