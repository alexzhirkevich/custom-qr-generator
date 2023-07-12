package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.Path
import android.graphics.RectF
import androidx.annotation.FloatRange
import androidx.core.graphics.minus
import com.github.alexzhirkevich.customqrgenerator.encoder.QrCodeMatrix
import com.github.alexzhirkevich.customqrgenerator.encoder.neighbors
import com.github.alexzhirkevich.customqrgenerator.encoder.toQrMatrix
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.google.zxing.qrcode.encoder.ByteMatrix

/**
 * Style of the qr-code eye frame.
 */
fun interface QrVectorFrameShape : QrVectorShapeModifier {

    /**
     * Size of the frame in QR code pixels
     * */
    val size : Int get() = 7

    object Default : QrVectorFrameShape by Rect(7)

    class Rect(override val size: Int = 7) : QrVectorFrameShape {

        override fun Path.shape(size: Float, neighbors: Neighbors) {
            val width = size / this@Rect.size.coerceAtLeast(1)
            addRect(0f, 0f, size, width, Path.Direction.CW)
            addRect(0f, 0f, width, size, Path.Direction.CW)
            addRect(size - width, 0f, size, size, Path.Direction.CW)
            addRect(0f, size - width, size, size, Path.Direction.CW)
        }
    }


    data class AsPixelShape(
        val pixelShape: QrVectorPixelShape,
        override val size: Int = 7
    ) : QrVectorFrameShape {

        override fun Path.shape(size: Float, neighbors: Neighbors) {
            val nSize = this@AsPixelShape.size.coerceAtLeast(1)
            val matrix = ByteMatrix(nSize, nSize)
                .toQrMatrix()

            repeat(nSize) { i ->
                repeat(nSize) { j ->
                    matrix[i, j] = if (i == 0 || j == 0 || i == nSize - 1 || j == nSize - 1)
                        QrCodeMatrix.PixelType.DarkPixel else QrCodeMatrix.PixelType.Background
                }
            }

            repeat(nSize) { i ->
                repeat(nSize) { j ->
                    if (matrix[i, j] == QrCodeMatrix.PixelType.DarkPixel)
                        addPath(
                            pixelShape.createPath(
                                size / nSize,
                                matrix.neighbors(i, j)
                            ),
                            size / nSize * i, size / nSize * j
                        )
                }
            }
        }
    }


    data class Circle(
        @FloatRange(from = 0.0) val width: Float = 1f,
        @FloatRange(from = 0.0) val radius: Float = 1f,
        override val size: Int = 7
    ) : QrVectorFrameShape {
        override fun Path.shape(size: Float, neighbors: Neighbors) {
            val width = (size / this@Circle.size.coerceAtLeast(1)) * width
            val radius = radius.coerceAtLeast(0f)
            addCircle(size / 2f, size / 2f, size / 2f * radius, Path.Direction.CW)
            addCircle(size / 2f, size / 2f, (size / 2f - width) * radius, Path.Direction.CCW)
        }
    }


    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        @FloatRange(from = 0.0) val width: Float = 1f,
        val topLeft: Boolean = true,
        val bottomLeft: Boolean = true,
        val topRight: Boolean = true,
        val bottomRight: Boolean = true,
        override val size: Int = 7
    ) : QrVectorFrameShape {
        override fun Path.shape(size: Float, neighbors: Neighbors) {

            val corner = corner.coerceIn(0f, .5f)

            val width = size / this@RoundCorners.size.coerceAtLeast(1) * width.coerceAtLeast(0f)

            val outerCornerSize = corner * size
            val innerCornerSize = corner * (size - (2 * width)).coerceAtLeast(0f)

            addRoundRect(
                RectF(0f, 0f, size, size),
                floatArrayOf(
                    if (topLeft) outerCornerSize else 0f,
                    if (topLeft) outerCornerSize else 0f,
                    if (topRight) outerCornerSize else 0f,
                    if (topRight) outerCornerSize else 0f,
                    if (bottomRight) outerCornerSize else 0f,
                    if (bottomRight) outerCornerSize else 0f,
                    if (bottomLeft) outerCornerSize else 0f,
                    if (bottomLeft) outerCornerSize else 0f,
                ),
                Path.Direction.CW
            )
            op(Path().apply {  addRoundRect(
                RectF(width, width, size - width, size - width),
                floatArrayOf(
                    if (topLeft) innerCornerSize else 0f,
                    if (topLeft) innerCornerSize else 0f,
                    if (topRight) innerCornerSize else 0f,
                    if (topRight) innerCornerSize else 0f,
                    if (bottomRight) innerCornerSize else 0f,
                    if (bottomRight) innerCornerSize else 0f,
                    if (bottomLeft) innerCornerSize else 0f,
                    if (bottomLeft) innerCornerSize else 0f,
                ),
                Path.Direction.CCW)
            }, Path.Op.XOR)
        }
    }
}