package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.github.alexzhirkevich.customqrgenerator.QrUtil
import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Color of the different QR code elements.
 * */
fun interface QrColor  {


    /**
     * @return Color of the [[i],[j]] pixel of current element
     * */
    @ColorInt
    operator fun invoke(i: Int, j: Int, width: Int, height: Int) : Int

    /**
     * Special color style.
     *
     * If it is applied to:
     *
     * - frame or ball - they will be painted as dark pixels.
     * - logo background - it will be painted as QR code background
     * - otherwise - becomes transparent
     * */
    @Serializable
    @SerialName("Unspecified")
    object Unspecified : QrColor by Solid(0)


    @Serializable
    @SerialName("Solid")
    data class Solid(@ColorInt val color : Int) : QrColor {

        @ColorInt
        override fun invoke(i: Int, j: Int, width: Int, height: Int): Int = color
    }


    @Serializable
    @SerialName("LinearGradient")
    data class LinearGradient(
        @ColorInt val startColor : Int,
        @ColorInt val endColor : Int,
        val orientation: Orientation
    ) : QrColor{

        enum class Orientation {
            Vertical, Horizontal, LeftDiagonal, RightDiagonal
        }

        @ColorInt
        override fun invoke(i: Int, j: Int, width: Int, height: Int): Int {
            val proportion = when (orientation){
                Orientation.Vertical -> 1f - j.toFloat()/ height
                Orientation.Horizontal -> 1f - i.toFloat()/ width
                Orientation.LeftDiagonal -> 1f - (i+j.toFloat())/(width+height)
                Orientation.RightDiagonal -> 1f - (i+ width -j.toFloat())/(width+height)
            }
            return QrUtil.mixColors(startColor, endColor, proportion.coerceIn(0f..1f))
        }
    }


    @Serializable
    @SerialName("SquareGradient")
    data class SquareGradient(
        val startColor : Int,
        val endColor : Int,
    ) : QrColor {

        @ColorInt
        override fun invoke(i: Int, j: Int, width: Int, height: Int): Int {
            val ti = minOf(i, width -i)
            val tj = minOf(j, height -j)
            val proportion = minOf(ti,tj) * 2f / minOf(width,height)

            return QrUtil.mixColors(startColor, endColor, proportion)
        }
    }


    @Serializable
    @SerialName("RhombusGradient")
    data class RhombusGradient(
        val startColor : Int,
        val endColor : Int,
    ) : QrColor {

        @ColorInt
        override fun invoke(i: Int, j: Int, width: Int, height: Int): Int {
            val ti = minOf(i, width -i)
            val tj = minOf(j, height -j)
            val proportion = ti.toFloat()/width + tj.toFloat()/height

            return QrUtil.mixColors(startColor, endColor, proportion)
        }
    }


    @Serializable
    @SerialName("RadialGradient")
    data class RadialGradient(
        @ColorInt val startColor : Int,
        @ColorInt val endColor : Int,
        @FloatRange(from = 0.0)
        val radius : Float = 1f
    ) : QrColor{

        @ColorInt
        override fun invoke(i: Int, j: Int, width: Int, height: Int): Int {

            val center = width / 2f
            val ti = minOf(i, width - i)
            val tj = minOf(j, height - j)
            val proportion = (sqrt((center-ti).pow(2) + (center-tj).pow(2)) /
                    center / radius.coerceAtLeast(Float.MIN_VALUE))

            return QrUtil.mixColors(startColor, endColor, proportion.coerceIn(0f..1f))
        }
    }


    @Serializable
    @SerialName("CrossingGradient")
    data class CrossingGradient(
        @ColorInt val colorLeftDiagonal : Int,
        @ColorInt val colorRightDiagonal : Int,
    ) : QrColor {

        @ColorInt
        override fun invoke(i: Int, j: Int, width: Int, height: Int): Int {
            val center = width /2f

            val color = if(i <= center && j <= center || i>=center && j >=center)
                colorLeftDiagonal else colorRightDiagonal

            val middleColor = QrUtil.mixColors(colorLeftDiagonal,colorRightDiagonal,.5f)

            val realI = minOf(i, width -i)
            val realJ = minOf(j, width -j)

            val imin = minOf(realI, (realI-center/2).toInt())
            val jmin = minOf(realJ,(realJ-center/2).toInt())

            return when {
                realI <= center/2 && realJ <= center/2 ->
                    color
                realI > center/2 && realJ < center/2 ->
                    LinearGradient(color,middleColor,LinearGradient.Orientation.Horizontal)
                        .invoke(imin, jmin,width/4, height /4)
                realI < center/2 && realJ > center/2 ->
                    LinearGradient(color,middleColor,LinearGradient.Orientation.Vertical)
                        .invoke(imin, jmin,width/4, height /4)
                else -> {
                    val order : (Int, Int) -> Int = if (
                        color == colorLeftDiagonal && colorLeftDiagonal > colorRightDiagonal ||
                        color == colorRightDiagonal && colorLeftDiagonal < colorRightDiagonal
                    ) ::minOf else ::maxOf
                    order(
                        LinearGradient(color, middleColor, LinearGradient.Orientation.Vertical)
                            .invoke(imin, jmin,width/4, height / 4),
                        LinearGradient(color, middleColor, LinearGradient.Orientation.Horizontal)
                            .invoke(imin, jmin,width/4, height / 4)
                    )
                }
            }
        }
    }

    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        @Suppress("unchecked_cast")
        override val defaultSerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModule {
                polymorphicDefaultSerializer(QrColor::class){
                    Unspecified.serializer() as SerializationStrategy<QrColor>
                }
                polymorphicDefaultDeserializer(QrColor::class) {
                    Unspecified.serializer()
                }
                polymorphic(QrColor::class) {
                    subclass(Unspecified::class)
                    subclass(Solid::class)
                    subclass(LinearGradient::class)
                    subclass(SquareGradient::class)
                    subclass(RhombusGradient::class)
                    subclass(RadialGradient::class)
                    subclass(CrossingGradient::class)
                }
            }
        }
    }
}

/**
 * Converts a 0xAARRGGBB [Long] to [ColorInt]
 * */
@ColorInt fun Long.toColor() : Int = Color(this)

/**
 * Converts a 0xAARRGGBB [Long] to [ColorInt]
 * */
@ColorInt fun Color(argb : Long) : Int {
    val a = (argb shr 24 and 0xff).toInt()
    val r = (argb shr 16 and 0xff).toInt()
    val g = (argb shr 8 and 0xff).toInt()
    val b = (argb and 0xff).toInt()
    return Color(a,r,g,b)
}

@ColorInt fun Color(
    @IntRange(from = 0, to = 255) a : Int,
    @IntRange(from = 0, to = 255) r : Int,
    @IntRange(from = 0, to = 255) g : Int,
    @IntRange(from = 0, to = 255) b : Int
) = (a.coerceIn(0,255) shl 24) or
        (r.coerceIn(0,255) shl 16) or
        (g.coerceIn(0,255) shl 8)  or
        b.coerceIn(0,2555)