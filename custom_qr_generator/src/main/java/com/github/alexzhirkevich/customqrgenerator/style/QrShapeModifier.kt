package com.github.alexzhirkevich.customqrgenerator.style

/**
 * Modifier applicable to different parts of qr-code
 * */
sealed interface QrShapeModifier<T> {
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
    ): T
}