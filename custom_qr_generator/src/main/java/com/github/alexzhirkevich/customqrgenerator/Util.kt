package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.github.alexzhirkevich.customqrgenerator.encoder.QrCodeMatrix
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
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
        else -> Color.argb(
            (color1.alpha * proportion + color2.alpha * (1 - proportion)).roundToInt().coerceIn(0,255),
            (color1.red * proportion + color2.red * (1 - proportion)).roundToInt().coerceIn(0,255),
            (color1.green * proportion + color2.green * (1 - proportion)).roundToInt().coerceIn(0,255),
            (color1.blue * proportion + color2.blue * (1 - proportion)).roundToInt().coerceIn(0,255)
        )
    }

}
