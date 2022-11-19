
package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange
import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import kotlinx.serialization.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.math.*


/**
 * Style of the qr-code pixels.
 * */
fun interface QrPixelShape : QrShapeModifier {

    @Serializable
    @SerialName("Default")
    object Default : QrPixelShape by DefaultShapeModifier
        .asPixelShape()

    @Serializable
    @SerialName("Circle")
    data class Circle(
        @FloatRange(from = .5, to = 1.0)
        private val size : Float = 1f
    ) : QrPixelShape by CircleShapeModifier(size)
        .asPixelShape()


    @Serializable
    @SerialName("Rhombus")
    object Rhombus : QrPixelShape by RhombusShapeModifier
        .asPixelShape()


    /**
     * If corner is true - it can be round depending on [Neighbors].
     * If corner is false - it will never be round.
     * */
    @Serializable
    @SerialName("RoundCorners")
    data class RoundCorners(
        val corner : Float = .5f,
        val topLeft : Boolean = true,
        val topRight : Boolean = true,
        val bottomLeft : Boolean = true,
        val bottomRight : Boolean = true
    ) : QrPixelShape by RoundCornersShapeModifier(
        corner = corner,
        useNeighbors = true,
        topLeft = topLeft,
        topRight = topRight,
        bottomLeft = bottomLeft,
        bottomRight = bottomRight
    ).asPixelShape()


    // TODO: fix
    /**
     * Doesn't work well with QrOptions.size < 512 and [sidePadding] > 0
     * */
    @Serializable
    @SerialName("RoundCornersHorizontal")
    class RoundCornersHorizontal(
        @FloatRange(from = .0, to = .5)
        val sidePadding : Float = 0f
    ) : QrPixelShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            neighbors: Neighbors
        ): Boolean = with(neighbors) {
            val padding = (elementSize * sidePadding).roundToInt()

            j in padding until elementSize - padding &&
                    RoundCornersShapeModifier.isRoundDark(
                        i = i,
                        j = j - padding,
                        //idk why even size here causes protruding sticks with low code size
                        elementSize = (elementSize - padding * 2)
                            .let { if (it % 2 == 1) it else it - 1 },
                        neighbors = neighbors,
                        corner = .5f,
                        useNeighbors = true,
                        topLeft = top.not(),
                        topRight = top.not(),
                        bottomLeft = bottom.not(),
                        bottomRight = bottom.not()
                    )
        }
    }


    // TODO: fix
    /**
     * Doesn't work well with QrOptions.size < 512 and [sidePadding] > 0
     * */
    @Serializable
    @SerialName("RoundCornersVertical")
    data class RoundCornersVertical(
        @FloatRange(from = .0, to = .5)
        val sidePadding : Float = 0f
    ) : QrPixelShape {
        override fun invoke(
            i: Int, j: Int, elementSize: Int,
            neighbors: Neighbors
        ): Boolean = with(neighbors) {

            val padding = (elementSize * sidePadding).roundToInt()

            i in padding until elementSize - padding &&
                    RoundCornersShapeModifier.isRoundDark(
                        i = i - padding,
                        j = j,
                        //idk why even size here causes protruding sticks with low code size
                        elementSize = (elementSize - padding * 2)
                            .let { if (it % 2 == 1) it else it - 1 },
                        neighbors = neighbors,
                        corner = .5f,
                        useNeighbors = true,
                        topLeft = left.not(),
                        topRight = right.not(),
                        bottomLeft = left.not(),
                        bottomRight = right.not()
                    )
        }
    }

    @Serializable
    @SerialName("Star")
    object Star : QrPixelShape by StarShapeModifier


    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        @Suppress("unchecked_cast")
        override val defaultSerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModule {
                polymorphicDefaultSerializer(QrPixelShape::class){
                    Default.serializer() as SerializationStrategy<QrPixelShape>
                }
                polymorphicDefaultDeserializer(QrPixelShape::class) {
                    Default.serializer()
                }
                polymorphic(QrPixelShape::class) {
                    subclass(Default::class)
                    subclass(Circle::class)
                    subclass(Rhombus::class)
                    subclass(RoundCorners::class)
                    subclass(RoundCornersVertical::class)
                    subclass(RoundCornersHorizontal::class)
                    subclass(Star::class)
                }
            }
        }
    }
}


fun QrShapeModifier.asPixelShape() : QrPixelShape = if (this is QrPixelShape) this else
    QrPixelShape { i, j, elementSize, neighbors ->
        this@asPixelShape
            .invoke(i, j, elementSize, neighbors)
    }
