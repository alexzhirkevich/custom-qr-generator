package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.IntRange
import com.github.alexzhirkevich.customqrgenerator.encoder.QrCodeMatrix

/**
 * Status of the neighbor qr-code pixels.
 *
 *
 * Drawable only: For frames/balls it describes neighbor frames/balls (for example: top left frame will always have bottom and right neighbors).
 * */
data class Neighbors(
    val topLeft : Boolean=false,
    val topRight : Boolean=false,
    val left : Boolean=false,
    val top : Boolean=false,
    val right : Boolean=false,
    val bottomLeft: Boolean=false,
    val bottom: Boolean=false,
    val bottomRight: Boolean=false,
) {

    var info : QrComponentInfo? = null
        private set

    constructor(
        topLeft : Boolean=false,
        topRight : Boolean=false,
        left : Boolean=false,
        top : Boolean=false,
        right : Boolean=false,
        bottomLeft: Boolean=false,
        bottom: Boolean=false,
        bottomRight: Boolean=false,
        info : QrComponentInfo? = null
    ) : this(topLeft, topRight, left, top, right, bottomLeft, bottom, bottomRight){
        this.info = info
    }

    companion object {
        val Empty = Neighbors()
    }

    val hasAny : Boolean
        get() = topLeft || topRight || left || top ||
            right || bottomLeft || bottom || bottomRight

    val hasAllNearest
        get() = top && bottom && left && right

    val hasAll : Boolean
        get() = topLeft && topRight && left && top &&
            right && bottomLeft && bottom && bottomRight

}

sealed interface QrComponentInfo {
    class SinglePixel(
        val type : QrCodeMatrix.PixelType
    ) : QrComponentInfo {

    }
}

internal fun Neighbors.Companion.forEyeWithNumber(number : Int, fourthEyeEnabled : Boolean) : Neighbors {
    return when (number) {
        0 -> Neighbors(bottom = true, right = true, bottomRight = fourthEyeEnabled)
        1 -> Neighbors(bottom = fourthEyeEnabled, left = true, bottomLeft = true)
        2 -> Neighbors(top = true, topRight = true, right = fourthEyeEnabled)
        3 -> {
            if (!fourthEyeEnabled) throw IllegalStateException("Fourth eye is disabled")
            Neighbors(top = true, left = true, topLeft = true)
        }

        else -> throw IllegalStateException("Incorrect eye number: $number")
    }
}