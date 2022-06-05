package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Style of the qr-code pixels.
 * Element size in 1 by default.
 * You can implement your own style by overriding [isDark] method.
 * @see QrModifier
 * */
interface QrPixelStyle : QrModifier {

    object Default : QrPixelStyle

    class Circle(
        @FloatRange(from = MIN_SIZE.toDouble(), to = MAX_SIZE.toDouble())
        private val size : Float = 1f
    ) : QrPixelStyle {

        companion object{
            const val MIN_SIZE = .5f
            const val MAX_SIZE = 1.0f
        }

        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int,
            neighbors: Neighbors
        ): Boolean {

            val center = elementSize/2.0
            return (sqrt((center-i)*(center-i) + (center-j)*(center-j)) <
                    center * size.coerceIn(MIN_SIZE, MAX_SIZE))
        }
    }

    object Rhombus : QrPixelStyle{
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int,
            neighbors: Neighbors
        ): Boolean {
            val center = elementSize/2.0
            return (i+j < center || abs(j-i) > center || i+j > 3*center).not()
        }
    }


    class RoundCornersIndependent(
        val topLeft : Boolean = true,
        val topRight : Boolean = true,
        val bottomLeft : Boolean = true,
        val bottomRight : Boolean = true
    ) : QrPixelStyle{
        override fun isDark(
            i: Int,
            j: Int,
            elementSize: Int,
            qrPixelSize: Int,
            neighbors: Neighbors
        ): Boolean {
            return QrBallStyle.RoundCorners(.5f,
            topLeft,topRight,bottomLeft,bottomRight).isDark(
                i, j, elementSize, qrPixelSize, neighbors
            )
        }
    }
    /**
     * If corner is true - it can be round depending on [Neighbors].
     * If corner is false - it will never be round.
     * */
    class RoundCorners(
        val topLeft : Boolean = true,
        val topRight : Boolean = true,
        val bottomLeft : Boolean = true,
        val bottomRight : Boolean = true
    ) : QrPixelStyle {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int,
            neighbors: Neighbors
        ): Boolean =
            isRoundDark(
                i, j, elementSize, qrPixelSize, neighbors,
                topLeft && neighbors.top.not() && neighbors.left.not(),
                topRight && neighbors.top.not() && neighbors.right.not(),
                bottomLeft && neighbors.bottom.not() && neighbors.left.not(),
                bottomRight && neighbors.bottom.not() && neighbors.right.not()
            )

        internal companion object {
            internal fun isRoundDark(
                i: Int, j: Int, elementSize: Int,
                qrPixelSize: Int,
                neighbors: Neighbors,
                topLeft : Boolean,
                topRight : Boolean,
                bottomLeft : Boolean,
                bottomRight : Boolean) : Boolean {
                if (neighbors.hasAny.not()){
                    return Circle(1f)
                        .isDark(i, j,elementSize, qrPixelSize, neighbors)
                }
                if (neighbors.hasAllNearest){
                    return Default
                        .isDark(i, j,elementSize, qrPixelSize, neighbors)
                }
                val cornerRadius = .25
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
                    else -> return QrFrameStyle.Default
                        .isDark(i, j,elementSize, qrPixelSize, neighbors)
                }
                return sqrt((x-i)*(x-i) + (y-j)*(y-j)) < sub
            }
        }
    }


    object RoundCornersHorizontal : QrPixelStyle {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int,
            neighbors: Neighbors
        ): Boolean = with(neighbors) {
            RoundCorners.isRoundDark(
                i, j, elementSize, qrPixelSize, neighbors,
                top.not(), top.not(), bottom.not(), bottom.not()
            )
        }
    }
    object RoundCornersVertical : QrPixelStyle {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int,
            neighbors: Neighbors
        ): Boolean = with(neighbors) {
            RoundCorners.isRoundDark(
                i, j, elementSize, qrPixelSize, neighbors,
                left.not(), right.not(), left.not(), right.not()
            )
        }
    }
}