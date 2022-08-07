package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Style of the qr-code eye frame.
 * (changing has no affect).
 * You can implement your own style by overriding [invoke] method.
 * Frame width should be equal to pixelSize.
 * @see QrShapeModifier
 * */
interface QrFrameShape : QrShapeModifier {


    object Default : QrFrameShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean {
            val size = elementSize
            return i in 0..qrPixelSize || j in 0..qrPixelSize ||
                    i in size-qrPixelSize..size || j in size- qrPixelSize .. size
        }
    }


    /**
     * Special style for QR code eye frame - frame pixels will be counted as qr pixels.
     * For example, [QrPixelShape.Circle] style will make eye frame look like a chaplet.
     * */
    data class AsPixelShape(val shape: QrPixelShape)
        : QrShapeModifierDelegate(
            delegate = Default + shape % {_, ps, _ -> ps}
        ), QrFrameShape


    object Circle : QrFrameShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean {
            val radius = elementSize / 2.0
            return sqrt((radius - i).pow(2) + (radius - j).pow(2)) in
                    radius - qrPixelSize .. radius
        }
    }


    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrFrameShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean {
            val cornerRadius = (.5f - corner.coerceIn(0f, .5f)) * elementSize
            val center = elementSize/2f

            val sub = center - cornerRadius
            val sum = center + cornerRadius

            val (x,y) = when{
                outer && i < sub && j < sub -> sub to sub
                horizontalOuter && i < sub && j > sum -> sub to sum
                verticalOuter && i > sum && j < sub -> sum to sub
                inner && i > sum && j > sum -> sum to sum
                else -> return Default.invoke(i, j,elementSize,qrPixelSize,neighbors)
            }
            return sqrt((x-i)*(x-i) + (y-j)*(y-j)) in sub-qrPixelSize .. sub
        }
    }
}


fun QrShapeModifier.asFrameShape() : QrFrameShape = if (this is QrFrameShape) this else
    object : QrFrameShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            qrPixelSize: Int, neighbors: Neighbors
        ): Boolean = this@asFrameShape
            .invoke(i, j, elementSize, qrPixelSize, neighbors)
    }