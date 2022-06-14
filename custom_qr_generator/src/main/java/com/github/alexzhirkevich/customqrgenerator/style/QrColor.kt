package com.github.alexzhirkevich.customqrgenerator.style

import android.graphics.Color
import androidx.annotation.ColorInt
import com.github.alexzhirkevich.customqrgenerator.QrUtil
import kotlin.math.sqrt

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

    class Solid(@ColorInt val color : Int) : QrColor {

        @ColorInt
        override fun invoke(i: Int, j: Int, elementSize: Int, qrPixelSize: Int): Int = color
    }

    class LinearGradient(
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
                Orientation.Vertical -> 1f - i.toFloat()/elementSize
                Orientation.Horizontal -> 1f - j.toFloat()/elementSize
                Orientation.LeftDiagonal -> 1f - i*j.toFloat()/elementSize/elementSize
                Orientation.RightDiagonal -> 1f - i*(elementSize-j).toFloat()/elementSize/elementSize
            }
            return QrUtil.mixColors(startColor, endColor, proportion.coerceIn(0f..1f))
        }
    }

    class RadialGradient(
        @ColorInt val centerColor : Int,
        @ColorInt val radiusColor : Int,
    ) : QrColor{

        @ColorInt
        override fun invoke(i: Int, j: Int, elementSize: Int, qrPixelSize: Int): Int {
            val center = elementSize/2f
            val proportion = 1f - sqrt((i-center) * (i-center) + (j-center) * (j-center))/center
            return QrUtil.mixColors(centerColor, radiusColor, proportion.coerceIn(0f..1f))
        }
    }

    class CrossingGradient(
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
                    LinearGradient(color,middleColor,LinearGradient.Orientation.Vertical)
                        .invoke(imin, jmin, elementSize/4, qrPixelSize)
                realI < center/2 && realJ > center/2 ->
                    LinearGradient(color,middleColor,LinearGradient.Orientation.Horizontal)
                        .invoke(imin, jmin, elementSize/4,qrPixelSize)
                else -> {
                    val order : (Int, Int) -> Int = if (color == colorLeftDiagonal)
                        ::maxOf else ::minOf
                    order(
                        LinearGradient(color, middleColor, LinearGradient.Orientation.Horizontal)
                            .invoke(imin, jmin, elementSize / 4, qrPixelSize),
                        LinearGradient(color, middleColor, LinearGradient.Orientation.Vertical)
                            .invoke(imin, jmin, elementSize / 4, qrPixelSize)
                    )
                }
            }
        }
    }
}