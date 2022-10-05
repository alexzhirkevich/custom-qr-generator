package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.Paint
import android.graphics.Shader
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import kotlin.math.sqrt

sealed interface QrVectorColor {

    fun createPaint(width: Float, height: Float): Paint

    object Unspecified : QrVectorColor by Solid(0)

    data class Solid(@ColorInt val color: Int) : QrVectorColor {
        override fun createPaint(width: Float, height: Float) = Paint().apply {
            color = this@Solid.color
            isAntiAlias = true
        }
    }

    data class LinearGradient(
        val colors: List<Pair<Float, Int>>,
        val orientation: Orientation
    ) : QrVectorColor {

        enum class Orientation(
            val start: (Float, Float) -> Pair<Float, Float>,
            val end: (Float, Float) -> Pair<Float, Float>
        ) {
            Vertical({ w, _ -> w / 2 to 0f }, { w, h -> w / 2 to h }),
            Horizontal({ _, h -> 0f to h / 2 }, { w, h -> w to h / 2 }),
            LeftDiagonal({ _, _ -> 0f to 0f }, { w, h -> w to h }),
            RightDiagonal({ _, h -> 0f to h }, { w, _ -> w to 0f })
        }

        override fun createPaint(width: Float, height: Float): Paint {
            val (x0, y0) = orientation.start(width, height)
            val (x1, y1) = orientation.end(width, height)
            return Paint().apply {
                shader = android.graphics.LinearGradient(
                    x0, y0, x1, y1,
                    colors.map { it.second }.toIntArray(),
                    colors.map { it.first }.toFloatArray(),
                    Shader.TileMode.CLAMP
                )
                isAntiAlias = true
            }
        }
    }

    data class RadialGradient(
        val colors: List<Pair<Float, Int>>,
        @FloatRange(from = 0.0)
        val radius: Float = sqrt(2f),
    ) : QrVectorColor {
        override fun createPaint(width: Float, height: Float): Paint = Paint().apply {
            shader = android.graphics.RadialGradient(
                width / 2, height / 2,
                maxOf(width, height) / 2 * radius.coerceAtLeast(0f),
                colors.map { it.second }.toIntArray(),
                colors.map { it.first }.toFloatArray(),
                Shader.TileMode.CLAMP
            )
            isAntiAlias = true
        }
    }

    data class SweepGradient(
        val colors: List<Pair<Float, Int>>
    ) : QrVectorColor {

        override fun createPaint(width: Float, height: Float): Paint = Paint().apply {
            shader = android.graphics.SweepGradient(
                width / 2, height / 2,
                colors.map { it.second }.toIntArray(),
                colors.map { it.first }.toFloatArray()
            )
        }
    }
}