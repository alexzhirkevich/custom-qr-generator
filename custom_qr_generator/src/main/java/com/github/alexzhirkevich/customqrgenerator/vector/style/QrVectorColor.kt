package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.alpha
import com.github.alexzhirkevich.customqrgenerator.style.Color
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor.Transparent.paint
import kotlin.math.sqrt
import kotlin.random.Random

enum class QrPaintMode {

    /**
     * Default behavior.
     *
     * - For dots(pixels): All pixels will be combined to a single path.
     * [QrVectorColor.createPaint] will be called 1 time with the size of the whole QR code payload.
     *
     * - For frames/balls: If color is not specified and pixels painting mode is [Combine] then
     * frame/ball paths will be combined with dots path.
     * Otherwise, [QrVectorColor.createPaint] will be called 3(4) times - for each corner
     * */
    Combine,

    /**
     * Allows to paint each part individually. CAN AFFECT PERFORMANCE IF APPLIED TO DOTS(PIXELS)
     *
     * - For dots(pixels): All pixels will be drawn separately.
     * [QrVectorColor.createPaint] will be called for each dark/light dot.
     *
     * - For frames/balls: Each frame/ball will be shaped and painted separately.
     * [QrVectorColor.createPaint] and [QrVectorShapeModifier.createPath] will be called 3(4) times - for each corner
     * */
    Separate
}

interface QrVectorColor {

    /**
     * Painting mode of the QR code element.
     * */
    val mode : QrPaintMode get() = QrPaintMode.Combine

    @Deprecated("Use Paint.paint(with,height) instead. Will be removed in 2.0",
        ReplaceWith("Paint().apply { paint(width, height) }", "android.graphics.Paint")
    )
    fun createPaint(width: Float, height: Float): Paint = Paint().apply {
        paint(width, height)
    }

    fun Paint.paint(width: Float, height: Float)

    object Transparent : QrVectorColor {
        override fun Paint.paint(width: Float, height: Float) {
            color = Color(0)
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST)
        }
    }

    /**
     * This color can cut out qr code part from resulting drawable.
     * Makes it transparent, ignoring background color and image.
     * */
    object Eraser : QrVectorColor {
        override fun Paint.paint(width: Float, height: Float) {
            alpha = 0
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        }
    }
    
    object Unspecified : QrVectorColor by Transparent


    data class Solid constructor(
        @ColorInt val color: Int,
    ) : QrVectorColor {
        override fun Paint.paint(width: Float, height: Float) {
            color = this@Solid.color
        }
    }

    class SolidRandom constructor(
        private val probabilities : List<Pair<Float, Int>>,
        private val random: Random = Random
    ) : QrVectorColor {

        private val _probabilities = mutableListOf<Pair<ClosedFloatingPointRange<Float>,Int>>()
        init {
            assert(probabilities.isNotEmpty()) {
                "SolidRandom color list can't be empty"
            }
            (listOf(0f) + probabilities.map { it.first }).reduceIndexed { index, sum, i ->
                _probabilities.add(sum..(sum + i) to probabilities[index - 1].second)
                sum + i
            }
        }

        constructor(
            @ColorInt colors : List<Int>,
        ) : this(colors.map { 1f to it }, Random)

        override val mode: QrPaintMode
            get() = QrPaintMode.Separate

        override fun Paint.paint(width: Float, height: Float) {
            val random = random.nextFloat() * _probabilities.last().first.endInclusive

            val idx = _probabilities.binarySearch {
                when {
                    random < it.first.start -> 1
                    random > it.first.endInclusive -> -1
                    else -> 0
                }
            }

            color = probabilities[idx].second
        }
    }


    data class LinearGradient constructor(
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

        override fun Paint.paint(width: Float, height: Float) {
            val (x0, y0) = orientation.start(width, height)
            val (x1, y1) = orientation.end(width, height)
            shader = android.graphics.LinearGradient(
                x0, y0, x1, y1,
                colors.map { it.second }.toIntArray(),
                colors.map { it.first }.toFloatArray(),
                Shader.TileMode.CLAMP
            )

        }
    }

    
    data class RadialGradient constructor(
        val colors: List<Pair<Float, Int>>,
        @FloatRange(from = 0.0)
        val radius: Float = sqrt(2f)
    ) : QrVectorColor {
        override fun Paint.paint(width: Float, height: Float) {
            shader = android.graphics.RadialGradient(
                width / 2, height / 2,
                maxOf(width, height) / 2 * radius.coerceAtLeast(0f),
                colors.map { it.second }.toIntArray(),
                colors.map { it.first }.toFloatArray(),
                Shader.TileMode.CLAMP
            )
        }
    }

    
    data class SweepGradient constructor(
        val colors: List<Pair<Float, Int>>,
    ) : QrVectorColor {

        override fun Paint.paint(width: Float, height: Float) {
            shader = android.graphics.SweepGradient(
                width / 2, height / 2,
                colors.map { it.second }.toIntArray(),
                colors.map { it.first }.toFloatArray()
            )
        }
    }
}

internal val QrVectorColor.isTransparent : Boolean
    get() = this is QrVectorColor.Transparent || this is QrVectorColor.Unspecified ||
            this is QrVectorColor.Solid && this.color.alpha == 0

internal val QrVectorColor.isSpecified : Boolean
    get() = this !is QrVectorColor.Unspecified