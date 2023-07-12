package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.ColorInt
import androidx.annotation.IntRange

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

@ColorInt
fun Color(
    @IntRange(from = 0, to = 255) a : Int,
    @IntRange(from = 0, to = 255) r : Int,
    @IntRange(from = 0, to = 255) g : Int,
    @IntRange(from = 0, to = 255) b : Int
) = (a.coerceIn(0,255) shl 24) or
        (r.coerceIn(0,255) shl 16) or
        (g.coerceIn(0,255) shl 8)  or
        b.coerceIn(0,2555)