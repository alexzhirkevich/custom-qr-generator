package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.Path
import androidx.annotation.FloatRange
import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import com.github.alexzhirkevich.customqrgenerator.encoder.QrCodeMatrix
import com.github.alexzhirkevich.customqrgenerator.encoder.neighbors
import com.github.alexzhirkevich.customqrgenerator.encoder.toQrMatrix
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.style.QrBallShape.AsPixelShape
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
 * Style of the qr-code eye internal ball.
 * */
interface QrVectorBallShape : QrVectorShapeModifier {


    @Serializable
    @SerialName("Default")
    object Default : QrVectorBallShape, QrVectorShapeModifier by DefaultVectorShape

    /**
     * Special style for QR code ball.
     *
     * [AsPixelShape] with the shape of dark pixels will be used.
     * */
    @Serializable
    @SerialName("AsDarkPixels")
    object AsDarkPixels : QrVectorBallShape {
        override fun createPath(size: Float, neighbors: Neighbors): Path {
            return Path()
        }
    }

    @Serializable
    @SerialName("AsPixelShape")
    class AsPixelShape(
        val pixelShape: QrVectorPixelShape
    ) : QrVectorBallShape {

        override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {

            val matrix =  ByteMatrix(3,3).apply { clear(1) }
                .toQrMatrix()
            repeat(3){ i ->
                repeat(3){ j ->
                    addPath(
                        pixelShape.createPath(
                            size / 3,
                            matrix.neighbors(i,j)
                        ),
                        size/3 * i, size/3 * j
                    )
                }
            }
        }
    }



    @Serializable
    @SerialName("Circle")
    data class Circle(
        @FloatRange(from = 0.0, to = 1.0) val size: Float
    ) : QrVectorBallShape, QrVectorShapeModifier by CircleVectorShape(size)

    @Serializable
    @SerialName("RoundCorners")
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

    @Serializable
    @SerialName("Rhombus")
    data class Rhombus(
        @FloatRange(from = 0.0, to = 1.0) private val scale : Float = 1f
    ) : QrVectorBallShape, QrVectorShapeModifier by RhombusVectorShape(scale)

    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        @Suppress("unchecked_cast")
        override val defaultSerializersModule: SerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModule {
                include(QrPixelShape.defaultSerializersModule)
                polymorphicDefaultSerializer(QrVectorBallShape::class){
                    Default.serializer() as SerializationStrategy<QrVectorBallShape>
                }
                polymorphicDefaultDeserializer(QrVectorBallShape::class) {
                    Default.serializer()
                }
                polymorphic(QrVectorBallShape::class){
                    subclass(Default::class)
                    subclass(AsDarkPixels::class)
                    subclass(Circle::class)
                    subclass(RoundCorners::class)
                    subclass(Rhombus::class)
                    subclass(AsPixelShape::class)
                }
            }
        }
    }
}