package com.github.alexzhirkevich.customqrgenerator.encoder
class QrCodeBitMatrix(val size : Int){

    enum class PixelType { Dot, Frame, Ball }

    private val bits = MutableList(size){
        MutableList(size){
            false
        }
    }

    private val types = MutableList(size){
        MutableList(size){
            PixelType.Dot
        }
    }

    operator fun get(i : Int, j : Int) : Pair<Boolean,PixelType> {
        return bits[i][j] to types[i][j]
    }

    operator fun set(i : Int, j : Int, value : Boolean, type: PixelType){
        bits[i][j] = value
        types[i][j] = type
    }
}