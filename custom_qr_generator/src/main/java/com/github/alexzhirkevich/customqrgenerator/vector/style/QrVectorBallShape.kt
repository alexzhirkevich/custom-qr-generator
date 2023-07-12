package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.Path
import androidx.annotation.FloatRange
import com.github.alexzhirkevich.customqrgenerator.encoder.neighbors
import com.github.alexzhirkevich.customqrgenerator.encoder.toQrMatrix
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.google.zxing.qrcode.encoder.ByteMatrix

/**
 * Style of the qr-code eye internal ball.
 * */
fun interface QrVectorBallShape : QrVectorShapeModifier {
    
    object Default : QrVectorBallShape, QrVectorShapeModifier by DefaultVectorShape

    data class AsPixelShape(
        val pixelShape: QrVectorPixelShape
    ) : QrVectorBallShape {

        override fun Path.shape(size: Float, neighbors: Neighbors) {
            val matrix = ByteMatrix(3, 3).apply { clear(1) }
                .toQrMatrix()
            repeat(3) { i ->
                repeat(3) { j ->
                    addPath(
                        pixelShape.createPath(
                            size / 3,
                            matrix.neighbors(i, j)
                        ),
                        size / 3 * i, size / 3 * j
                    )
                }
            }
        }
    }

    class Rect(
        @FloatRange(from = 0.0, to = 1.0) val size: Float = 1f
    ) : QrVectorBallShape, QrVectorShapeModifier by RectVectorShape(size)

    data class Circle(
        @FloatRange(from = 0.0, to = 1.0) val size: Float = 1f
    ) : QrVectorBallShape, QrVectorShapeModifier by CircleVectorShape(size)

    
    data class RoundCorners(
        @FloatRange(from = 0.0, to = .5) val radius: Float,
        val topLeft: Boolean = true,
        val bottomLeft: Boolean = true,
        val topRight: Boolean = true,
        val bottomRight: Boolean = true,
    ) : QrVectorBallShape, QrVectorShapeModifier by RoundCornersVectorShape(
        cornerRadius = radius,
        withNeighbors = false,
        topLeft = topLeft,
        bottomLeft = bottomLeft,
        topRight = topRight,
        bottomRight = bottomRight
    )

    
    data class Rhombus(
        @FloatRange(from = 0.0, to = 1.0) private val scale : Float = 1f
    ) : QrVectorBallShape, QrVectorShapeModifier by RhombusVectorShape(scale)
}