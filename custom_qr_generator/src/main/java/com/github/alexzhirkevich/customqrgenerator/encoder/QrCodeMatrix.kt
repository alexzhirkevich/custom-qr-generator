package com.github.alexzhirkevich.customqrgenerator.encoder

class QrCodeMatrix(val size : Int){

    enum class PixelType { DarkPixel, LightPixel, Background, Logo }

    private val types = MutableList(size * size) {
        PixelType.Background
    }

    operator fun get(i : Int, j : Int) : PixelType {

        val outOfBound = when {
            i !in 0 until size -> i
            j !in 0 until size -> j
            else -> null
        }

        if (outOfBound != null)
            throw IndexOutOfBoundsException(
                "Index $outOfBound is out of 0..${size -1} matrix bound"
            )

        return types[i + j * size]
    }

    operator fun set(i: Int, j: Int, type: PixelType){

        val outOfBound = when {
            i !in 0 until size -> i
            j !in 0 until size -> j
            else -> null
        }

        if (outOfBound != null)
            throw IndexOutOfBoundsException(
                "Index $outOfBound is out of 0..${size -1} matrix bound"
            )


        types[i + j*size ] = type
    }
}