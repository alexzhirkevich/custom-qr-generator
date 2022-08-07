package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Style of the qr-code eye internal ball.
 * You can implement your own style by overriding [invoke] method.
 * @see QrShapeModifier
 * */
interface QrBallShape : QrShapeModifier {

    object Default : QrShapeModifierDelegate(
        delegate = DefaultShapeModifier
    ), QrBallShape


    /**
     * Special style for QR code ball - ball pixels will be counted as qr pixels.
     * For example, [QrPixelShape.Circle] style will make qr-code ball look like a square of 9 balls.
     * */
    data class AsPixelShape(val shape: QrPixelShape)
        : QrShapeModifierDelegate(
            delegate = Default + shape % { _, ps, _ -> ps }
        ), QrBallShape


    data class Circle(@FloatRange(from = .75, to = 1.0) private val size : Float = 1f) :
        QrShapeModifierDelegate(
            delegate = QrPixelShape.Circle(size)
        ), QrBallShape


    object Rhombus : QrShapeModifierDelegate(
        delegate = QrPixelShape.Rhombus
    ), QrBallShape


    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        val outer: Boolean = true,
        val horizontalOuter: Boolean = true,
        val verticalOuter: Boolean = true,
        val inner: Boolean = true,
    ) : QrBallShape {
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
                else -> return Default
                    .invoke(i, j,elementSize, qrPixelSize, neighbors)
            }
            return sqrt((x-i).pow(2) + (y-j).pow(2)) < sub
        }
    }
}

fun QrShapeModifier.asBallShape() : QrBallShape = if (this is QrBallShape) this else
    object : QrBallShape, QrShapeModifier by this{}