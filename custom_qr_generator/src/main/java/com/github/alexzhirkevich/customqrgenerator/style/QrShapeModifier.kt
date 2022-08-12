package com.github.alexzhirkevich.customqrgenerator.style

/**
 * Modifier applicable to different parts of qr-code
 * */
sealed interface QrShapeModifier {
    /**
     * @param i current row of [elementSize] x [elementSize] matrix
     * @param j current column [elementSize] x [elementSize] matrix
     * @param elementSize size of the element in pixels.
     * @param qrPixelSize size of 1 qr-code dot in pixels
     * @param neighbors status of the neighbor qr-code pixels
     * */
    operator fun invoke(
        i: Int, j: Int, elementSize: Int,
        neighbors: Neighbors
    ): Boolean
}

operator fun QrShapeModifier.plus(other : QrShapeModifier): QrShapeModifier =
    ShapeModifierSum(this, other)

operator fun QrShapeModifier.rem(
    remRuntime : (elemSize : Int, neighbors : Neighbors) -> Int
) : QrShapeModifier = ShapeModifierRemRuntime(this, remRuntime)

operator fun QrShapeModifier.rem(rem : Int) : QrShapeModifier =
    ShapeModifierRem(this, rem)



internal object DefaultShapeModifier : QrShapeModifier {
    override fun invoke(
        i: Int, j: Int, elementSize: Int,
        neighbors: Neighbors
    ): Boolean  = true
}

private class ShapeModifierSum(
    private val a : QrShapeModifier,
    private val b : QrShapeModifier
) : QrShapeModifier {
    override fun invoke(
        i: Int, j: Int, elementSize: Int,
        neighbors: Neighbors
    ): Boolean {
        return a(i, j, elementSize, neighbors) &&
                b(i, j, elementSize, neighbors)
    }
}

private class ShapeModifierRem(
    private val modifier: QrShapeModifier,
    private val rem: Int
) : QrShapeModifier {
    override fun invoke(
        i: Int, j: Int, elementSize: Int,
        neighbors: Neighbors
    ): Boolean {
        return modifier.invoke(i % rem, j % rem, rem, neighbors)
    }
}

private class ShapeModifierRemRuntime(
    private val modifier: QrShapeModifier,
    private val remRuntime : (elemSize : Int, neighbors : Neighbors) -> Int
) : QrShapeModifier {
    override fun invoke(
        i: Int, j: Int, elementSize: Int,
        neighbors: Neighbors
    ): Boolean {
        val rem = remRuntime(elementSize, neighbors)
        return modifier.invoke(i % rem, j % rem, rem, neighbors)
    }
}

