package com.github.alexzhirkevich.customqrgenerator.style

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.graphics.applyCanvas

interface QrCanvasColor {
    fun draw(canvas: Canvas)
}

fun QrCanvasColor.toQrColor(elementSize : Int) : QrColor =
    QrCanvasColorToQrColor(this, elementSize)


private class QrCanvasColorToQrColor(
    qrCanvasColor: QrCanvasColor,
    private val size: Int
) : QrColor {

    private val pixels : IntArray by lazy {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        bitmap.applyCanvas(qrCanvasColor::draw)
        val pixels = IntArray(size * size)
        bitmap.getPixels(pixels,0,size,0,0,size,size)
        bitmap.recycle()
        pixels
    }

    override fun invoke(i: Int, j: Int, elementSize: Int): Int {

        val scale = size / elementSize.toFloat()
        val sI = (i * scale).toInt()
        val sJ = (j * scale).toInt()

        return pixels[sI + size * sJ]
    }

}