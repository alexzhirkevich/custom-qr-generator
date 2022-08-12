package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange
import kotlin.math.*


/**
 * Style of the qr-code pixels.
 * */
interface QrPixelShape : QrShapeModifier {


    object Default : QrShapeModifierDelegate(
        delegate = DefaultShapeModifier
    ), QrPixelShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            neighbors: Neighbors
        ): Boolean = true
    }


    data class Circle(
        @FloatRange(from = .5, to = 1.0)
        private val size : Float = 1f
    ) : QrPixelShape {

        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            neighbors: Neighbors
        ): Boolean {

            val center = elementSize/2f
            return (sqrt((center-i).pow(2) + (center-j).pow(2)) <
                    center * size.coerceIn(0f, 1f))
        }
    }


    object Rhombus : QrPixelShape{
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            neighbors: Neighbors
        ): Boolean {
            val center = elementSize/2f
            return (i+j <= center || abs(j-i) >= center || i+j >= 3*center).not()
        }
    }

    /**
     * If corner is true - it can be round depending on [Neighbors].
     * If corner is false - it will never be round.
     * */
    data class RoundCorners(
        val topLeft : Boolean = true,
        val topRight : Boolean = true,
        val bottomLeft : Boolean = true,
        val bottomRight : Boolean = true
    ) : QrPixelShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            neighbors: Neighbors
        ): Boolean = isRoundDark(
            i, j, elementSize, neighbors,
            topLeft && neighbors.top.not() && neighbors.left.not(),
            topRight && neighbors.top.not() && neighbors.right.not(),
            bottomLeft && neighbors.bottom.not() && neighbors.left.not(),
            bottomRight && neighbors.bottom.not() && neighbors.right.not()
        )

        companion object {

            private val circle = Circle(1f)

            internal fun isRoundDark(
                i: Int, j: Int, elementSize: Int,
                neighbors: Neighbors,
                topLeft: Boolean,
                topRight: Boolean,
                bottomLeft: Boolean,
                bottomRight: Boolean
            ) : Boolean {
                if (neighbors.hasAny.not()){
                    return circle
                        .invoke(i, j, elementSize, neighbors)
                }
                if (neighbors.hasAllNearest){
                    return Default
                        .invoke(i, j, elementSize, neighbors)
                }
                val cornerRadius = .25f
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
                    else -> return Default
                        .invoke(i, j, elementSize, neighbors)
                }
                return sqrt((x-i).pow(2) + (y-j).pow(2)) < sub
            }
        }
    }


    // TODO: fix
    /**
     * Doesn't work well with QrOptions.size < 512 and [sidePadding] > 0
     * */
    class RoundCornersHorizontal(
        @FloatRange(from = .0, to = .5)
        val sidePadding : Float = 0f
    ) : QrPixelShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            neighbors: Neighbors
        ): Boolean = with(neighbors) {
            val padding = (elementSize * sidePadding).roundToInt()

            j in padding until elementSize - padding &&
            RoundCorners.isRoundDark(
                i, j-padding,
                //idk why even size here causes protruding sticks with low code size
                (elementSize-padding*2).let { if (it % 2 == 1) it else it -1 },
                neighbors, top.not(),
                top.not(), bottom.not(), bottom.not()
            )
        }
    }


    // TODO: fix
    /**
     * Doesn't work well with QrOptions.size < 512 and [sidePadding] > 0
     * */
    data class RoundCornersVertical(
        @FloatRange(from = .0, to = .5)
        val sidePadding : Float = 0f
    ) : QrPixelShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            neighbors: Neighbors
        ): Boolean = with(neighbors) {

            val padding = (elementSize * sidePadding).roundToInt()

            i in padding until elementSize - padding &&
            RoundCorners.isRoundDark(
                i-padding, j,
                //idk why even size here causes protruding sticks with low code size
                (elementSize-padding*2).let { if (it % 2 == 1) it else it -1 },
                neighbors, left.not(),
                right.not(), left.not(), right.not()
            )
        }
    }


    object Star : QrPixelShape {
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
}

fun QrShapeModifier.asPixelShape() : QrPixelShape = if (this is QrPixelShape) this else
    object : QrPixelShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            neighbors: Neighbors
        ): Boolean = this@asPixelShape
            .invoke(i, j, elementSize, neighbors)
    }