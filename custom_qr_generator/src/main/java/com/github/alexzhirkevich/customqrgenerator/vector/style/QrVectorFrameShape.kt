package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.Path
import android.graphics.RectF
import androidx.annotation.FloatRange
import androidx.core.graphics.minus
import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import com.github.alexzhirkevich.customqrgenerator.encoder.QrCodeMatrix
import com.github.alexzhirkevich.customqrgenerator.encoder.neighbors
import com.github.alexzhirkevich.customqrgenerator.encoder.toQrMatrix
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.style.QrPixelShape
import com.google.zxing.qrcode.encoder.ByteMatrix
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Style of the qr-code eye frame.
 */
interface QrVectorFrameShape : QrVectorShapeModifier {

    @Serializable
    @SerialName("Default")
    object Default : QrVectorFrameShape {

        override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {
            val width = size/7f
            addRect(0f,0f,size,width,Path.Direction.CW)
            addRect(0f,0f,width,size,Path.Direction.CW)
            addRect(size-width,0f,size,size,Path.Direction.CW)
            addRect(0f,size-width,size,size,Path.Direction.CW)
        }
    }

    /**
     * Special style for QR code frame.
     *
     * [AsPixelShape] with the shape of dark pixels will be used.
     * */
    @Serializable
    @SerialName("AsDarkPixels")
    object AsDarkPixels : QrVectorFrameShape {

        override fun createPath(size: Float, neighbors: Neighbors): Path {
            return Path()
        }
    }

    @Serializable
    @SerialName("AsPixelShape")
    data class AsPixelShape(
        val pixelShape: QrVectorPixelShape
    ) : QrVectorFrameShape {

        override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {

            val matrix =  ByteMatrix(7,7)
                .toQrMatrix()

            repeat(7) { i ->
                repeat(7) { j ->
                    matrix[i,j] = if (i == 0 || j == 0 || i == 6 || j == 6)
                        QrCodeMatrix.PixelType.DarkPixel else QrCodeMatrix.PixelType.Background
                }
            }

            repeat(7){ i ->
                repeat(7){ j ->
                    if (matrix[i,j] == QrCodeMatrix.PixelType.DarkPixel)
                        addPath(
                            pixelShape.createPath(
                                size / 7,
                                matrix.neighbors(i,j)
                            ),
                            size/7 * i, size/7 * j
                        )
                }
            }
        }
    }


    @Serializable
    @SerialName("Circle")
    data class Circle(
        @FloatRange(from = 0.0) val width : Float = 1f,
        @FloatRange(from = 0.0) val radius : Float = 1f
    ) : QrVectorFrameShape {
        override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {
            val width = (size/7f) * width
            val radius = radius.coerceAtLeast(0f)
            addCircle(size/2f, size/2f, size/2f * radius, Path.Direction.CW)
            addCircle(size/2f,size/2f,(size/2f - width) * radius, Path.Direction.CCW )
        }
    }

    @Serializable
    @SerialName("RoundCorners")
    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        @FloatRange(from = 0.0) val width: Float = 1f,
        val topLeft: Boolean = true,
        val bottomLeft: Boolean = true,
        val topRight: Boolean = true,
        val bottomRight: Boolean = true,
    ) : QrVectorFrameShape {
        override fun createPath(size: Float, neighbors: Neighbors): Path {

            val width = size / 7f * width.coerceAtLeast(0f)

            val outerCornerSize = corner * size
            val innerCornerSize = corner * (size - 4 * width)

            return Path().apply {
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
            } - Path().apply {
                addRoundRect(
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
                    Path.Direction.CCW
                )
            }
        }
    }

    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        @Suppress("unchecked_cast")
        override val defaultSerializersModule: SerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModule {
                polymorphicDefaultSerializer(QrVectorFrameShape::class){
                    Default.serializer() as SerializationStrategy<QrVectorFrameShape>
                }
                polymorphicDefaultDeserializer(QrVectorFrameShape::class) {
                    Default.serializer()
                }
                polymorphic(QrVectorFrameShape::class){
                    subclass(Default::class)
                    subclass(AsDarkPixels::class)
                    subclass(AsPixelShape::class)
                    subclass(Circle::class)
                    subclass(RoundCorners::class)
                }
            }
        }
    }
}