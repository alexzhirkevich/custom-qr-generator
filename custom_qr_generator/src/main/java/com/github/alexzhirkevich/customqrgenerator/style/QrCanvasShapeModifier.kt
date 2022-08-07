package com.github.alexzhirkevich.customqrgenerator.style

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.applyCanvas

/**
 * Create custom shape modifier by drawing on [Canvas].
 * This wrapper can be converted to [QrShapeModifier] using
 * [QrCanvasShapeModifier.toShapeModifier] function
 * */
interface QrCanvasShapeModifier {

    /**
     * @param canvas canvas to draw shape
     * @param drawPaint paint that should be used to draw a shape.
     * Any other [Paint] with the same [Color] can be used.
     * @param erasePaint paint that should be used to erase a shape.
     * Any other [Paint] with [Color] different from [drawPaint] can be used.
     *
     * */
    fun draw(canvas: Canvas, drawPaint : Paint, erasePaint : Paint)
}

/**
 * Convert [QrCanvasShapeModifier] to [QrShapeModifier].
 * The [size] should be >= than QrOptions size. Otherwise,
 * shapes quality will be lower than expected.
 */
fun QrCanvasShapeModifier.toShapeModifier(size : Int) = object : QrShapeModifier {

    private val drawPaint = Paint().apply {
        color = Color.BLACK
    }
    private val erasePaint = Paint().apply {
        color = Color.WHITE
    }

    private val pixels by lazy(LazyThreadSafetyMode.NONE) {
        val pixels = IntArray(size * size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            eraseColor(erasePaint.color)
        }.applyCanvas { draw(this, drawPaint, erasePaint) }

        bitmap.getPixels(pixels, 0, size, 0,0, size,size)
        bitmap.recycle()
        pixels
    }

    override fun invoke(
        i: Int, j: Int, elementSize: Int,
        qrPixelSize: Int, neighbors: Neighbors
    ): Boolean {

        val scale = size / elementSize.toFloat()
        val sI = (i * scale).toInt()
        val sJ = (j * scale).toInt()

        return pixels[sI + size * sJ] == drawPaint.color
    }
}