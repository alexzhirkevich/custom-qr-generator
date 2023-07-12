package com.github.alexzhirkevich.customqrgenerator.encoder

import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.style.QrComponentInfo
import com.google.zxing.qrcode.encoder.ByteMatrix

class QrCodeMatrix(val size : Int){

    enum class PixelType {
        DarkPixel,
        LightPixel,
        Background,
        Logo,
        VersionEye,
        TimingLine,
    }

    private var types = MutableList(size * size) {
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

    operator fun set(i: Int, j: Int, type: PixelType) {

        val outOfBound = when {
            i !in 0 until size -> i
            j !in 0 until size -> j
            else -> null
        }

        if (outOfBound != null)
            throw IndexOutOfBoundsException(
                "Index $outOfBound is out of 0..${size - 1} matrix bound"
            )

        types[i + j * size] = type
    }

    fun copy() : QrCodeMatrix = QrCodeMatrix(size).apply {
        types = this@QrCodeMatrix.types.toMutableList()
    }
}

internal fun QrCodeMatrix.neighbors(i : Int, j : Int, info : QrComponentInfo? = null) : Neighbors {

    fun cmp(i2 : Int, j2 : Int) = kotlin.runCatching {
        this[i2,j2] == this[i,j]
    }.getOrDefault(false)

    return Neighbors(
        topLeft = cmp(i - 1, j - 1),
        topRight = cmp(i + 1, j - 1),
        left = cmp(i-1, j),
        top = cmp(i, j-1),
        right = cmp(i+1, j),
        bottomLeft = cmp(i-1, j + 1),
        bottom = cmp(i, j+1),
        bottomRight = cmp(i+1, j + 1),
        info = info
    )
}

internal fun QrCodeMatrix.neighborsReversed(i : Int, j : Int) : Neighbors {

    fun cmp(i2 : Int, j2 : Int) = kotlin.runCatching {
        this[i2,j2] == this[i,j]
    }.getOrDefault(false)

    return Neighbors(
        topLeft = cmp(i - 1, j - 1),
        topRight = cmp(i - 1, j + 1),
        left = cmp(i, j - 1),
        top = cmp(i - 1, j),
        right = cmp(i, j + 1),
        bottomLeft = cmp(i+1, j - 1),
        bottom = cmp(i+1, j),
        bottomRight = cmp(i+1, j + 1)
    )
}
fun ByteMatrix.toQrMatrix() : QrCodeMatrix {
    if (width != height)
        throw IllegalStateException("Non-square qr byte matrix")

    return QrCodeMatrix(width).apply {
        for (i in 0 until width){
            for (j in 0 until width){
                this[i,j] = if (this@toQrMatrix[i,j].toInt() == 1)
                    QrCodeMatrix.PixelType.DarkPixel
                else QrCodeMatrix.PixelType.LightPixel
            }
        }
    }
}
