package com.github.alexzhirkevich.customqrgenerator.style

import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Color for QR code pixels. Allows to paint every pixel independently.
 * Can be solid only, cause [invoke] function is called 1 time for every
 * QR code pixel. Works like [QrColor] for other elements
 * */
fun interface QrColorSeparatePixels : QrColor {

    /**
     * Color of [[i],[j]] QR code pixel.
     *
     * @param width number of QR code pixels in row
     * @param height number of QR code pixels in column
     * */
    override fun invoke(i: Int, j: Int, width: Int, height: Int): Int

    /**
     * Randomly paint QR pixels in solid color
     *
     * @property colors map of QR pixel colors to their probabilities
     * */
    @Serializable
    data class Random(
        val colors : Map<Int, Float>
    ) : QrColorSeparatePixels {

        private val sorted = colors.toList().sortedBy { it.second }
        private val sum = colors.values.sum()

        override fun invoke(i: Int, j: Int, width: Int, height: Int): Int {
            if (colors.isEmpty())
                return 0
            val random = kotlin.random.Random.nextFloat() * sum

            var cSum = 0f
            for ((k,v) in sorted){
                cSum += v
                if (cSum > random)
                    return k
            }
            return sorted.last().first
        }
    }
    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        override val defaultSerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModule {
                polymorphic(QrColor::class) {
                    subclass(Random::class)
                }
            }
        }
    }
}
