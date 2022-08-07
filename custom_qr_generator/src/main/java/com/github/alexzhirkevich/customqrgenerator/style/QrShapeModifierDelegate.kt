package com.github.alexzhirkevich.customqrgenerator.style

sealed class QrShapeModifierDelegate (
    private val delegate : QrShapeModifier
) : QrShapeModifier {

    override fun invoke(
        i: Int,
        j: Int,
        elementSize: Int,
        qrPixelSize: Int,
        neighbors: Neighbors
    ): Boolean = delegate.invoke(i, j, elementSize, qrPixelSize, neighbors)
}