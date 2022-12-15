package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.Path
import android.graphics.RectF
import androidx.annotation.FloatRange
import androidx.core.graphics.*
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import kotlin.math.sqrt

internal object DefaultVectorShape : QrVectorShapeModifier {


    override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {
        addRect(0f,0f, size,size, Path.Direction.CW)
    }
}

internal class CircleVectorShape(
    @FloatRange(from = 0.0, to = 1.0) val size: Float
    ) : QrVectorShapeModifier {


    override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {
        addCircle(size/2f, size/2f, size/2 * this@CircleVectorShape.size.coerceIn(0f,1f), Path.Direction.CW)
    }
}

internal class RoundCornersVectorShape(
    @FloatRange(from = 0.0, to = 0.5) val cornerRadius : Float, val withNeighbors : Boolean
)  : QrVectorShapeModifier {


    override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {

        val corner = cornerRadius.coerceIn(0f,.5f) * size

        if (withNeighbors)
            addRoundRect(
                RectF(0f,0f,size, size),
                floatArrayOf(
                    if (neighbors.top.not() && neighbors.left.not()) corner else 0f,
                    if (neighbors.top.not() && neighbors.left.not()) corner else 0f,
                    if (neighbors.top.not() && neighbors.right.not()) corner else 0f,
                    if (neighbors.top.not() && neighbors.right.not()) corner else 0f,
                    if (neighbors.bottom.not() && neighbors.right.not()) corner else 0f,
                    if (neighbors.bottom.not() && neighbors.right.not()) corner else 0f,
                    if (neighbors.bottom.not() && neighbors.left.not()) corner else 0f,
                    if (neighbors.bottom.not() && neighbors.left.not()) corner else 0f,
                ),
                Path.Direction.CW
            )
        else addRoundRect(RectF(0f,0f,size, size),
                corner,
                corner,
                Path.Direction.CW
            )
    }
}

internal class RoundCornersVerticalVectorShape(
    @FloatRange(from = 0.0, to = 1.0) val width : Float
) : QrVectorShapeModifier {

    override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {
        if (neighbors.top){
            addRect(0f,0f, size, size/2f, Path.Direction.CW)
        } else {
            addCircle(size/2, size/2, size/2f, Path.Direction.CW)
        }
        if (neighbors.bottom){
            addRect(0f,size/2f, size, size, Path.Direction.CW)
        } else {
            addCircle(size/2, size/2, size/2f, Path.Direction.CW)
        }
    }
}

internal class RoundCornersHorizontalVectorShape(
    @FloatRange(from = 0.0, to = 1.0) val width : Float
) : QrVectorShapeModifier {


    override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {

        if (neighbors.left){
            addRect(0f,0f, size/2, size, Path.Direction.CW)
        } else {
            addCircle(size/2, size/2, size/2f, Path.Direction.CW)
        }
        if (neighbors.right){
            addRect(size/2,0f, size, size, Path.Direction.CW)
        } else {
            addCircle(size/2, size/2, size/2f, Path.Direction.CW)
        }
    }
}

internal object StarVectorShape : QrVectorShapeModifier {

    override fun createPath(size: Float, neighbors: Neighbors): Path =
        Path().apply { addRect(0f, 0f, size, size, Path.Direction.CW) } -
            Path().apply {
                repeat(4) {
                    addCircle(size, size, size / 2, Path.Direction.CW)
                    transform(rotationMatrix(90f, size/2, size/2))
                }
            }
}

internal class RhombusVectorShape(
    @FloatRange(from = 0.0, to = 1.0) private val scale : Float
) : QrVectorShapeModifier {

    override fun createPath(size: Float, neighbors: Neighbors): Path =
        Path().apply {

            addRect(0f, 0f, size, size, Path.Direction.CW)

            val s = 1 / sqrt(2f)
            transform(
                scaleMatrix(
                    sx = s,
                    sy = s
                )
            )
            transform(
                translationMatrix(
                    size * (1 - s) / 2,
                    size * (1 - s) / 2
                )
            )
            transform(
                scaleMatrix(
                    sx = scale.coerceIn(0f, 1f),
                    sy = scale.coerceIn(0f, 1f)
                )
            )
            transform(rotationMatrix(45f, size / 2f, size / 2f))

        }
}
