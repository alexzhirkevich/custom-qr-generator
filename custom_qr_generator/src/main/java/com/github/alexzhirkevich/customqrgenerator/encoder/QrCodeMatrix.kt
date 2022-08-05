package com.github.alexzhirkevich.customqrgenerator.encoder

class QrCodeMatrix(val size : Int){

    enum class PixelType { DarkPixel, LightPixel, Background }

    private val types = MutableList(size * size) {
        PixelType.Background
    }

    operator fun get(i : Int, j : Int) : PixelType {
        return types[i + j * size]
    }

    operator fun set(i: Int, j: Int, type: PixelType){
        types[i + j*size ] = type
    }
}