package com.github.alexzhirkevich.customqrgenerator.style

/**
 * Modifier applicable to different parts of qr-code
 * */
sealed interface QrModifier {
    /**
     * @param i current row of ([elementSize] * [pixelSize]) x ([elementSize] * [pixelSize]) matrix
     * @param j current column of ([elementSize] * [pixelSize]) x ([elementSize] * [pixelSize]) matrix
     * @param elementSize size of the element in qr-code pixels.
     * Multiply by [pixelSize] to calculate size in bitmap pixels
     * @param pixelSize size of 1 qr-code pixel in bitmap pixels
     * @param neighbors status of the neighbor qr-code pixels
     * */
    fun isDark(i: Int, j: Int, elementSize: Int, qrPixelSize: Int, neighbors: Neighbors): Boolean = true
}