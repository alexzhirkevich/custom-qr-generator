package com.github.alexzhirkevich.customqrgenerator.style

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

    object Circle : QrPixelStyle {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            pixelSize: Int, neighbors: Neighbors
        ): Boolean {
            val center = elementSize*pixelSize/2.0
            return  (sqrt((center-i)*(center-i) + (center-j)*(center-j)) < center)
        }

    }

    object Rhombus : QrPixelStyle{
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            pixelSize: Int, neighbors: Neighbors
        ): Boolean {
            val center = elementSize*pixelSize/2.0
            return (i+j < center || abs(j-i) > center || i+j > 3*center).not()
        }
    }

    object RoundCorners : QrPixelStyle {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            pixelSize: Int, neighbors: Neighbors
        ): Boolean = RoundCornersHorizontal.isDark(i, j, elementSize, pixelSize, neighbors) ||
                RoundCornersVertical.isDark(i, j, elementSize, pixelSize, neighbors)
    }

    object RoundCornersHorizontal : QrPixelStyle {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            pixelSize: Int, neighbors: Neighbors
        ): Boolean {
            if (neighbors.hasAny.not()){
                return Circle.isDark(i, j, elementSize, pixelSize, neighbors)
            }
            if (neighbors.hasAllNearest){
                return Default.isDark(i, j, elementSize, pixelSize, neighbors)
            }
            val cornerRadius = .25
            val center = elementSize*pixelSize/2f

            val sub = center - cornerRadius
            val sum = center + cornerRadius

            val (x,y) = when{

                neighbors.top.not() && i < sub && j < sub -> sub to sub
                neighbors.top.not() && i < sub && j > sum -> sub to sum
                neighbors.bottom.not() &&
                        i > sum && j < sub -> sum to sub
                neighbors.bottom.not() &&
                        i > sum && j > sum -> sum to sum
                else -> return QrFrameStyle.Default.isDark(i, j, elementSize, pixelSize, neighbors)
            }
            return sqrt((x-i)*(x-i) + (y-j)*(y-j)) < sub
        }
    }
    object RoundCornersVertical : QrPixelStyle {
        override fun isDark(
            i: Int, j: Int, elementSize: Int,
            pixelSize: Int, neighbors: Neighbors
        ): Boolean {
            if (neighbors.hasAny.not()){
                return Circle.isDark(i, j, elementSize, pixelSize, neighbors)
            }
            if (neighbors.hasAllNearest){
                return Default.isDark(i, j, elementSize, pixelSize, neighbors)
            }
            val cornerRadius = .25
            val center = elementSize*pixelSize/2f

            val sub = center - cornerRadius
            val sum = center + cornerRadius

            val (x,y) = when{

                neighbors.left.not() && i < sub && j < sub -> sub to sub
                neighbors.right.not() && i < sub && j > sum -> sub to sum
                neighbors.left.not() &&
                        i > sum && j < sub -> sum to sub
                neighbors.right.not() &&
                        i > sum && j > sum -> sum to sum
                else -> return QrFrameStyle.Default.isDark(i, j, elementSize, pixelSize, neighbors)
            }
            return sqrt((x-i)*(x-i) + (y-j)*(y-j)) < sub
        }
    }
}