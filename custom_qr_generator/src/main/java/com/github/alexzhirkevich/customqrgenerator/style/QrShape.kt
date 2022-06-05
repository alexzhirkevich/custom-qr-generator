package com.github.alexzhirkevich.customqrgenerator.style

import com.google.zxing.qrcode.encoder.ByteMatrix
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

interface QrShape {

    fun apply(byteMatrix: ByteMatrix) : ByteMatrix

    object Default : QrShape {
        override fun apply(byteMatrix: ByteMatrix): ByteMatrix =
            byteMatrix
    }

    class Circle(private val random : Random = Random) : QrShape{
        override fun apply(byteMatrix: ByteMatrix): ByteMatrix = with(byteMatrix){
            if (width != height)
                throw IllegalStateException("Non-square ByteMatrix can not be extended to round")

            val added = (width *1.05 * sqrt(2.0)).roundToInt()/4

            val newSize = width + 2*added
            val newMatrix = ByteMatrix(newSize,newSize)

            val center = newSize / 2f

            for (i in 0 until newSize) {
                for (j in 0 until newSize) {
                    if (random.nextBoolean() &&
                        (i < added-1 ||
                                j < added-1 ||
                                i > added + width ||
                                j > added + width ) &&
                        sqrt((center-i) *(center-i)+(center-j)*(center-j)) <center
                    ){
                        newMatrix.set(i, j, 1)

                    }
                }
            }

            for(i in 0 until width){
                for(j in 0 until height){
                    newMatrix[added+i,added+j] = this[i,j]
                }
            }
            return newMatrix
        }
    }
}