package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.Path
import android.graphics.RectF
import androidx.annotation.FloatRange
import androidx.core.graphics.and
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors

internal object DefaultVectorShape : QrVectorShapeModifier {

    override val isDependOnNeighbors: Boolean get() = false

    override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {
        addRect(0f,0f, size,size, Path.Direction.CW)
    }
}

internal class CircleVectorShape(
    @FloatRange(from = 0.0, to = 1.0) val size: Float
    ) : QrVectorShapeModifier {

    override val isDependOnNeighbors: Boolean get() = false

    override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {
        addCircle(size/2f, size/2f, size/2 * this@CircleVectorShape.size.coerceIn(0f,1f), Path.Direction.CW)
    }
}

internal class RoundCornersVectorShape(
    @FloatRange(from = 0.0, to = 0.5) val cornerRadius : Float,
)  : QrVectorShapeModifier {

    override val isDependOnNeighbors: Boolean get() = true

    override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {

        val corner = cornerRadius.coerceIn(0f,.5f) * size

//        if (isDependOnNeighbors)
            addRoundRect(
                RectF(0f,0f,size, size),
                floatArrayOf(
                    if (neighbors.top.not() && neighbors.left.not()) corner else 0f,
                    if (neighbors.top.not() && neighbors.left.not()) corner else 0f,
                    if (neighbors.bottom.not() && neighbors.left.not()) corner else 0f,
                    if (neighbors.bottom.not() && neighbors.left.not()) corner else 0f,
                    if (neighbors.bottom.not() && neighbors.right.not()) corner else 0f,
                    if (neighbors.bottom.not() && neighbors.right.not()) corner else 0f,
                    if (neighbors.top.not() && neighbors.right.not()) corner else 0f,
                    if (neighbors.top.not() && neighbors.right.not()) corner else 0f,
                ),
                Path.Direction.CW
            )
//        else addRoundRect(RectF(0f,0f,size, size),
//                corner,
//                corner,
//                Path.Direction.CW
//            )
    }
}
