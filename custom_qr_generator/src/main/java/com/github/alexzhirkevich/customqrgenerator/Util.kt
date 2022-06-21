package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.google.zxing.qrcode.encoder.ByteMatrix
import kotlin.math.roundToInt

internal object QrUtil {

    @ColorInt
    fun mixColors(
        @ColorInt color1 : Int,
        @ColorInt color2 : Int,
        @FloatRange(from = .0, to = 1.0) proportion : Float
    ) : Int = when {
        color1.alpha == 0  -> color2
        color2.alpha == 0 -> color1
        else -> Color.rgb(
            (color1.red * proportion + color2.red * (1 - proportion)).roundToInt().coerceIn(0,255),
            (color1.green * proportion + color2.green * (1 - proportion)).roundToInt().coerceIn(0,255),
            (color1.blue * proportion + color2.blue * (1 - proportion)).roundToInt().coerceIn(0,255)
        )
    }

}

internal fun ByteMatrix.neighbors(i : Int, j : Int) : Neighbors {
    val topLeft = kotlin.runCatching {
        this[i - 1, j - 1].toInt() == 1
    }.getOrDefault(false)
    val topRight = kotlin.runCatching {
        this[i - 1, j + 1].toInt() == 1
    }.getOrDefault(false)
    val top = kotlin.runCatching {
        this[i - 1, j].toInt() == 1
    }.getOrDefault(false)
    val left = kotlin.runCatching {
        this[i, j - 1].toInt() == 1
    }.getOrDefault(false)
    val right = kotlin.runCatching {
        this[i, j + 1].toInt() == 1
    }.getOrDefault(false)
    val bottomLeft = kotlin.runCatching {
        this[i+1, j - 1].toInt() == 1
    }.getOrDefault(false)
    val bottomRight = kotlin.runCatching {
        this[i+1, j + 1].toInt() == 1
    }.getOrDefault(false)
    val bottom = kotlin.runCatching {
        this[i+1, j].toInt() == 1
    }.getOrDefault(false)
    return Neighbors(
        topLeft, topRight, left, top, right, bottomLeft, bottom, bottomRight
    )
}