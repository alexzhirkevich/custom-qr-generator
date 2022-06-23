package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange
import com.google.zxing.qrcode.encoder.ByteMatrix
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random


/**
 * Shape of the QR-code pattern.
 * */
interface QrShape {

    /**
     * Transform actual matrix or create new with BIGGER size.
     * Matrix reducing causes [IllegalStateException].
     * @param [byteMatrix] square matrix of qr-code pixels. 1 if pixel is set else 0
     * */
    fun apply(byteMatrix: ByteMatrix) : ByteMatrix

    /**
     * Decide if pixel fits inside a QR code shape.
     * [modifiedByteMatrix] is already changed by [apply].
     * */
    fun pixelInShape(i : Int, j : Int, modifiedByteMatrix: ByteMatrix) : Boolean

    object Default : QrShape {
        override fun apply(byteMatrix: ByteMatrix) = byteMatrix

        override fun pixelInShape(i: Int, j: Int, modifiedByteMatrix: ByteMatrix)  = true
    }

    data class Circle(
        @FloatRange(from = 1.0, to = 2.0)
        val padding : Float = 1.1f,
        private val random : Random = Random
    ) : QrShape {

        override fun pixelInShape(i: Int, j: Int, modifiedByteMatrix: ByteMatrix): Boolean =
            with(modifiedByteMatrix) {
                val center = width/2f
                return sqrt((center - i) * (center - i) + (center-j) * (center - j)) <= center
            }

        override fun apply(byteMatrix: ByteMatrix): ByteMatrix = with(byteMatrix){
            if (width != height)
                throw IllegalStateException("Non-square ByteMatrix can not be extended to round")

            val padding = padding.coerceIn(1f,2f)
            val added = (((width * padding * sqrt(2.0)) - width)/2).roundToInt()

            val newSize = width + 2*added
            val newMatrix = ByteMatrix(newSize,newSize)

            val center = newSize / 2f

            for (i in 0 until newSize) {
                for (j in 0 until newSize) {
                    if (random.nextBoolean() &&
                        (i <= added-1 ||
                                j <= added-1 ||
                                i >= added + width ||
                                j >= added + width ) &&
                        sqrt((center-i) *(center-i)+(center-j)*(center-j)) <= center
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