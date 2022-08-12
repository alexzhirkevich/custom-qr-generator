package com.github.alexzhirkevich.customqrgenerator.style

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.github.alexzhirkevich.customqrgenerator.QrUtil


@ColorInt
fun Long.toColor() : Int = Color(this)
/**
 * Converts a 0xAARRGGBB [Long] to [ColorInt]
 * */
@ColorInt
fun Color(argb : Long) : Int = Color.argb(
    (argb shr 24 and 0xff).toInt(),
    (argb shr 16 and 0xff).toInt(),
    (argb shr 8 and 0xff).toInt(),
    (argb and 0xff).toInt(),
)

@ColorInt
fun Color(
    @IntRange(from = 0, to = 255) a : Int,
    @IntRange(from = 0, to = 255) r : Int,
    @IntRange(from = 0, to = 255) g : Int,
    @IntRange(from = 0, to = 255) b : Int
) : Int = Color.argb(
    a,r,g,b
)

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
    operator fun invoke(i: Int, j: Int, elementSize: Int) : Int

    /**
     * Special color style. If it applied to pixels - they will be transparent.
     * Other elements will be painted in pixel style.
     * */
    object Unspecified : QrColor {

        @ColorInt
        override fun invoke(i: Int, j: Int, elementSize: Int): Int =
            Color.TRANSPARENT
    }

    data class Solid(@ColorInt val color : Int) : QrColor {

        @ColorInt
        override fun invoke(i: Int, j: Int, elementSize: Int): Int = color
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
        override fun invoke(i: Int, j: Int, elementSize: Int): Int {
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
        override fun invoke(i: Int, j: Int, elementSize: Int): Int {
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
        override fun invoke(i: Int, j: Int, elementSize: Int): Int {
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
        override fun invoke(i: Int, j: Int, elementSize: Int): Int {
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
                        .invoke(imin, jmin, elementSize/4)
                realI < center/2 && realJ > center/2 ->
                    LinearGradient(color,middleColor,LinearGradient.Orientation.Vertical)
                        .invoke(imin, jmin, elementSize/4)
                else -> {
                    val order : (Int, Int) -> Int = if (
                        color == colorLeftDiagonal && colorLeftDiagonal > colorRightDiagonal ||
                        color == colorRightDiagonal && colorLeftDiagonal < colorRightDiagonal
                    ) ::minOf else ::maxOf
                    order(
                        LinearGradient(color, middleColor, LinearGradient.Orientation.Vertical)
                            .invoke(imin, jmin, elementSize / 4),
                        LinearGradient(color, middleColor, LinearGradient.Orientation.Horizontal)
                            .invoke(imin, jmin, elementSize / 4)
                    )
                }
            }
        }
    }
}