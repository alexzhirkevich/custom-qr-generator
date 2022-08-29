package com.github.alexzhirkevich.customqrgenerator.style

import kotlinx.serialization.Serializable

/**
 * Status of the neighbor qr-code pixels
 * */
@Serializable
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

