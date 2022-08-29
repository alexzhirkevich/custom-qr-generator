package com.github.alexzhirkevich.customqrgenerator.style

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.graphics.applyCanvas

fun interface QrCanvasColor {
    fun draw(canvas: Canvas)
}

fun QrCanvasColor.toQrColor(width : Int, height:Int) : QrColor =
    QrCanvasColorToQrColor(this, width,height)


private class QrCanvasColorToQrColor(
    qrCanvasColor: QrCanvasColor,
    private val width: Int,
    private val height: Int
) : QrColor {

    private val pixels : IntArray by lazy {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.applyCanvas(qrCanvasColor::draw)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels,0,width,0,0,width,height)
        bitmap.recycle()
        pixels
    }

    override fun invoke(i: Int, j: Int, width: Int, height: Int): Int {

        val scaleX = this.width / width.toFloat()
        val scaleY = this.height / height.toFloat()
        val sI = (i * scaleX).toInt()
        val sJ = (j * scaleY).toInt()

        return pixels[sI + this.width * sJ]
    }

}