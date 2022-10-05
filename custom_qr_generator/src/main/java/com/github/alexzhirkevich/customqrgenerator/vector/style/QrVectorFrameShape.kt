package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.Path
import android.graphics.RectF
import androidx.annotation.FloatRange
import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.style.QrFrameShape
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

    override val isDependOnNeighbors: Boolean get() = false

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

    @Serializable
    @SerialName("Circle")
    class Circle(
        @FloatRange(from = 0.0) val width : Float = 1f,
        @FloatRange(from = 0.0) val radius : Float = 1f
    ) : QrVectorFrameShape {
        override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {
            val width = (size/7f) * width
            addCircle(size/2f, size/2f, size/2f * radius, Path.Direction.CW)
            addCircle(size/2f,size/2f,(size/2f - width/2f) * radius, Path.Direction.CCW )
        }
    }

    @Serializable
    @SerialName("RoundCorners")
    data class RoundCorners(
        @FloatRange(from = 0.0, to = 0.5) val corner: Float,
        @FloatRange(from = 0.0) val width: Float = 1f,
    ) : QrVectorFrameShape {
        override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {

            val width = size/6f * width
            val half = width/2f

            addRoundRect(
                RectF(0f,0f,size, size),
                corner * size,
                corner * size,
                Path.Direction.CW
            )
            addRoundRect(
                RectF(half, half,size-half , size-half),
                corner * (size -width),
                corner * (size -width),
                Path.Direction.CCW
            )
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
                    subclass(Circle::class)
                    subclass(RoundCorners::class)
                }
            }
        }
    }
}