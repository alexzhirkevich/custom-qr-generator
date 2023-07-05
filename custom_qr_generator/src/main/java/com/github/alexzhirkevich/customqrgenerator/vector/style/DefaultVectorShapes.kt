package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.Path
import android.graphics.RectF
import androidx.annotation.FloatRange
import androidx.core.graphics.*
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import kotlin.math.sqrt

internal object DefaultVectorShape : QrVectorShapeModifier {


    override fun Path.shape(size: Float, neighbors: Neighbors): Path = apply {
        addRect(0f,0f, size,size, Path.Direction.CW)
    }
}

internal class CircleVectorShape(
    @FloatRange(from = 0.0, to = 1.0) val size: Float
    ) : QrVectorShapeModifier {
    override fun Path.shape(size: Float, neighbors: Neighbors): Path  = apply {
        addCircle(size/2f, size/2f,
            size/2 * this@CircleVectorShape.size.coerceIn(0f,1f), Path.Direction.CW)
    }
}

internal class RoundCornersVectorShape(
    @FloatRange(from = 0.0, to = 0.5)
    val cornerRadius : Float,
    val withNeighbors : Boolean,
    val topLeft: Boolean = true,
    val bottomLeft: Boolean = true,
    val topRight: Boolean = true,
    val bottomRight: Boolean = true,
)  : QrVectorShapeModifier {
    override fun Path.shape(size: Float, neighbors: Neighbors): Path {
        val corner = cornerRadius.coerceIn(0f, .5f) * size

        addRoundRect(
            RectF(0f, 0f, size, size),
            floatArrayOf(
                if (topLeft && (withNeighbors.not() || neighbors.top.not() && neighbors.left.not())) corner else 0f,
                if (topLeft && (withNeighbors.not() || neighbors.top.not() && neighbors.left.not())) corner else 0f,
                if (topRight && (withNeighbors.not() || neighbors.top.not() && neighbors.right.not())) corner else 0f,
                if (topRight && (withNeighbors.not() || neighbors.top.not() && neighbors.right.not())) corner else 0f,
                if (bottomRight && (withNeighbors.not() || neighbors.bottom.not() && neighbors.right.not())) corner else 0f,
                if (bottomRight && (withNeighbors.not() || neighbors.bottom.not() && neighbors.right.not())) corner else 0f,
                if (bottomLeft && (withNeighbors.not() || neighbors.bottom.not() && neighbors.left.not())) corner else 0f,
                if (bottomLeft && (withNeighbors.not() || neighbors.bottom.not() && neighbors.left.not())) corner else 0f,
            ),
            Path.Direction.CW
        )
        return this
    }
}

internal class RoundCornersVerticalVectorShape(
    @FloatRange(from = 0.0, to = 1.0) val radius : Float
) : QrVectorShapeModifier {
    override fun Path.shape(size: Float, neighbors: Neighbors): Path {
        val padding = (size * (1 - radius.coerceIn(0f,1f)))

        if (neighbors.top){
            addRect(padding,0f, size-padding, size/2f, Path.Direction.CW)
        } else {
            addCircle(size/2, size/2, size/2f-padding, Path.Direction.CW)
        }
        if (neighbors.bottom){
            addRect(padding,size/2f, size-padding, size, Path.Direction.CW)
        } else {
            addCircle(size/2, size/2, size/2f-padding, Path.Direction.CW)
        }
        return this
    }
}

internal class RoundCornersHorizontalVectorShape(
    @FloatRange(from = 0.0, to = 1.0) val radius : Float
) : QrVectorShapeModifier {

    override fun Path.shape(size: Float, neighbors: Neighbors): Path = apply {
        val padding = (size * (1 - radius.coerceIn(0f,1f)))

        if (neighbors.left){
            addRect(0f,padding, size/2, size-padding, Path.Direction.CW)
        } else {
            addCircle(size/2, size/2, size/2f-padding, Path.Direction.CW)
        }
        if (neighbors.right){
            addRect(size/2,padding, size, size-padding, Path.Direction.CW)
        } else {
            addCircle(size/2, size/2, size/2f-padding, Path.Direction.CW)
        }
    }
}

internal object StarVectorShape : QrVectorShapeModifier {

    override fun Path.shape(size: Float, neighbors: Neighbors): Path {
        return Path().apply {
            addRect(0f, 0f, size, size, Path.Direction.CW)
        } - Path().apply {
            repeat(4) {
                addCircle(size, size, size / 2, Path.Direction.CW)
                transform(rotationMatrix(90f, size / 2, size / 2))
            }
        }
    }
}

internal class RhombusVectorShape(
    @FloatRange(from = 0.0, to = 1.0) private val scale : Float
) : QrVectorShapeModifier {

    override fun Path.shape(size: Float, neighbors: Neighbors) : Path = apply {
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
