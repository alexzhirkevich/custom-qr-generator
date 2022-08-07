package com.github.alexzhirkevich.customqrgenerator.style

/**
 * Modifier applicable to different parts of qr-code
 * */
interface QrShapeModifier {
    /**
     * @param i current row of [elementSize] x [elementSize] matrix
     * @param j current column [elementSize] x [elementSize] matrix
     * @param elementSize size of the element in pixels.
     * @param qrPixelSize size of 1 qr-code dot in pixels
     * @param neighbors status of the neighbor qr-code pixels
     * */
    operator fun invoke(
        i: Int, j: Int, elementSize: Int,
        qrPixelSize: Int, neighbors: Neighbors
    ): Boolean
}


internal object DefaultShapeModifier : QrShapeModifier {
    override fun invoke(
        i: Int, j: Int, elementSize: Int,
        qrPixelSize: Int, neighbors: Neighbors
    ): Boolean  = true
}


operator fun QrShapeModifier.plus(other : QrShapeModifier): QrShapeModifier =
    object : QrShapeModifier {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean = this@plus.invoke(i, j, elementSize, qrPixelSize, neighbors) &&
                other.invoke(i, j, elementSize, qrPixelSize, neighbors)
    }


operator fun QrShapeModifier.rem(
    remRuntime : (elemSize : Int, pixelSize : Int, neighbors : Neighbors) -> Int
) = object : QrShapeModifier {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean {
            val rem = remRuntime(elementSize, qrPixelSize, neighbors)
            return this@rem.invoke(i % rem, j % rem, rem, qrPixelSize, neighbors)
        }
    }


operator fun QrShapeModifier.rem(rem : Int) : QrShapeModifier =
    object : QrShapeModifier {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean = this@rem.invoke(i % rem, j % rem, elementSize % rem, qrPixelSize, neighbors)
    }

