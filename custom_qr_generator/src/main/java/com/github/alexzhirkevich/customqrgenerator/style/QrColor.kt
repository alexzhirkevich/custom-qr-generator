package com.github.alexzhirkevich.customqrgenerator.style

import android.graphics.Color
import androidx.annotation.ColorInt
import com.github.alexzhirkevich.customqrgenerator.QrUtil

/**
 * Color of the different QR code elements.
 * Supports alpha channel.
 * Return value of [invoke] is [ColorInt].
 * */
interface QrColor  {


    /**
     * @return Color of the [[i],[j]] pixel of current element
     * */
    @ColorInt
    operator fun invoke(i: Int, j: Int, elementSize: Int, qrPixelSize: Int) : Int

    /**
     * Special color style. If it applied to pixels - they will be transparent.
     * Other elements will be painted in pixel style.
     * */
    object Unspecified : QrColor {

        @ColorInt
        override fun invoke(i: Int, j: Int, elementSize: Int, qrPixelSize: Int): Int =
            Color.TRANSPARENT
    }

    data class Solid(@ColorInt val color : Int) : QrColor {

        @ColorInt
        override fun invoke(i: Int, j: Int, elementSize: Int, qrPixelSize: Int): Int = color
    }

    data class LinearGradient(
        @ColorInt val startColor : Int,
        @ColorInt val endColor : Int,
        val orientation: Orientation
    ) : QrColor{

        enum class Orientation {
            Vertical, Horizontal, LeftDiagonal, RightDiagonal
        }

        @ColorInt
        override fun invoke(i: Int, j: Int, elementSize: Int, qrPixelSize: Int): Int {
            val proportion = when (orientation){
                Orientation.Vertical -> 1f - j.toFloat()/elementSize
                Orientation.Horizontal -> 1f - i.toFloat()/elementSize
                Orientation.LeftDiagonal -> 1f - (i+j.toFloat())/2/elementSize
                Orientation.RightDiagonal -> 1f - (i+elementSize-j.toFloat())/2/elementSize
            }
            return QrUtil.mixColors(startColor, endColor, proportion.coerceIn(0f..1f))
        }
    }

    data class SquareGradient(
        val startColor : Int,
        val endColor : Int,
    ) : QrColor {
        override fun invoke(i: Int, j: Int, elementSize: Int, qrPixelSize: Int): Int {
            val ti = minOf(i, elementSize-i)
            val tj = minOf(j, elementSize-j)
            val proportion = minOf(ti,tj) * 2f / elementSize

            return QrUtil.mixColors(startColor, endColor, proportion)
        }
    }

    data class RadialGradient(
        @ColorInt val startColor : Int,
        @ColorInt val endColor : Int,
    ) : QrColor{

        @ColorInt
        override fun invoke(i: Int, j: Int, elementSize: Int, qrPixelSize: Int): Int {
            val center = elementSize/2f
            val ti = minOf(i, elementSize-i)
            val tj = minOf(j, elementSize-j)
            val proportion = (ti + tj) / (2f* center)
            return QrUtil.mixColors(startColor, endColor, proportion.coerceIn(0f..1f))
        }
    }

    data class CrossingGradient(
        @ColorInt val colorLeftDiagonal : Int,
        @ColorInt val colorRightDiagonal : Int,
    ) : QrColor {

        @ColorInt
        override fun invoke(i: Int, j: Int, elementSize: Int, qrPixelSize: Int): Int {
            val center = elementSize/2f

            val color = if(i <= center && j <= center || i>=center && j >=center)
                colorLeftDiagonal else colorRightDiagonal

            val middleColor = QrUtil.mixColors(colorLeftDiagonal,colorRightDiagonal,.5f)

            val realI = minOf(i,elementSize-i)
            val realJ = minOf(j, elementSize-j)

            val imin = minOf(realI, (realI-center/2).toInt())
            val jmin = minOf(realJ,(realJ-center/2).toInt())

            return when {
                realI <= center/2 && realJ <= center/2 ->
                    color
                realI > center/2 && realJ < center/2 ->
                    LinearGradient(color,middleColor,LinearGradient.Orientation.Horizontal)
                        .invoke(imin, jmin, elementSize/4, qrPixelSize)
                realI < center/2 && realJ > center/2 ->
                    LinearGradient(color,middleColor,LinearGradient.Orientation.Vertical)
                        .invoke(imin, jmin, elementSize/4,qrPixelSize)
                else -> {
                    val order : (Int, Int) -> Int = if (
                        color == colorLeftDiagonal && colorLeftDiagonal > colorRightDiagonal ||
                        color == colorRightDiagonal && colorLeftDiagonal < colorRightDiagonal
                    ) ::minOf else ::maxOf
                    order(
                        LinearGradient(color, middleColor, LinearGradient.Orientation.Vertical)
                            .invoke(imin, jmin, elementSize / 4, qrPixelSize),
                        LinearGradient(color, middleColor, LinearGradient.Orientation.Horizontal)
                            .invoke(imin, jmin, elementSize / 4, qrPixelSize)
                    )
                }
            }
        }
    }
}