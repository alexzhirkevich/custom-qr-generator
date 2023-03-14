@file:Suppress("deprecation")

package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

internal object DefaultShapeModifier : QrShapeModifier {
    override fun invoke(
        i: Int, j: Int, elementSize: Int,
        neighbors: Neighbors
    ): Boolean  = true
}

internal class CircleShapeModifier(
    @FloatRange(from = .5, to = 1.0)
    private val size : Float = 1f
) : QrShapeModifier {
    override fun invoke(i: Int, j: Int, elementSize: Int, neighbors: Neighbors): Boolean {
        val center = elementSize/2f
        return (sqrt((center-i).pow(2) + (center-j).pow(2)) <
                center * size.coerceIn(0f, 1f))
    }
}

internal object RhombusShapeModifier : QrShapeModifier {
    override fun invoke(
        i: Int, j: Int, elementSize: Int,
        neighbors: Neighbors
    ): Boolean {
        val center = elementSize/2f
        return (i+j <= center || abs(j-i) >= center || i+j >= 3*center).not()
    }
}

internal class RoundCornersShapeModifier(
    @FloatRange(from = 0.0, to = 0.5) private val corner: Float,
    private val useNeighbors: Boolean,
    private val topLeft: Boolean,
    private val topRight: Boolean,
    private val bottomLeft: Boolean,
    private val bottomRight: Boolean,
) : QrShapeModifier {

    override fun invoke(
        i: Int, j: Int, elementSize: Int,
        neighbors: Neighbors
    ): Boolean = isRoundDark(
        i = i,
        j = j,
        elementSize = elementSize,
        neighbors = neighbors,
        corner = corner,
        useNeighbors = useNeighbors,
        topLeft = topLeft && (useNeighbors.not() || neighbors.top.not() && neighbors.left.not()),
        topRight = topRight && (useNeighbors.not() ||neighbors.top.not() && neighbors.right.not()),
        bottomLeft = bottomLeft && (useNeighbors.not() ||neighbors.bottom.not() && neighbors.left.not()),
        bottomRight = bottomRight && (useNeighbors.not() || neighbors.bottom.not() && neighbors.right.not())
    )

    companion object{
        fun isRoundDark(
            i: Int, j: Int,
            elementSize: Int,
            neighbors: Neighbors,
            corner: Float,
            useNeighbors: Boolean,
            topLeft: Boolean,
            topRight: Boolean,
            bottomLeft: Boolean,
            bottomRight: Boolean
        ) : Boolean {
            if (useNeighbors) {
                if (neighbors.hasAny.not() &&
                    corner in .5f - Float.MIN_VALUE .. .5f + Float.MIN_VALUE
                ) {
                    return QrPixelShape.Circle(1f)
                        .invoke(i, j, elementSize, neighbors)
                }
                if (neighbors.hasAllNearest) {
                    return QrPixelShape.Default
                        .invoke(i, j, elementSize, neighbors)
                }
            }
            val cornerRadius = (.5f - corner.coerceIn(0f, .5f)) * elementSize
            val center = elementSize/2f

            val sub = center - cornerRadius
            val sum = center + cornerRadius

            val (x,y) = when{
                topLeft && i < sub && j < sub -> sub to sub
                topRight && i < sub && j > sum -> sub to sum
                bottomLeft &&
                        i > sum && j < sub -> sum to sub
                bottomRight &&
                        i > sum && j > sum -> sum to sum
                else -> return QrPixelShape.Default
                    .invoke(i, j, elementSize, neighbors)
            }
            return sqrt((x-i).pow(2) + (y-j).pow(2)) < sub
        }
    }
}

internal object StarShapeModifier : QrShapeModifier, QrPixelShape {
    override fun invoke(
        i: Int, j: Int, elementSize: Int,
        neighbors: Neighbors
    ): Boolean {
        val radius = elementSize/2f

        val i2 = minOf(i, elementSize-i)
        val j2 = minOf(j, elementSize-j)

        return sqrt((i2 * i2).toDouble() + (j2 * j2)) > radius
    }
}