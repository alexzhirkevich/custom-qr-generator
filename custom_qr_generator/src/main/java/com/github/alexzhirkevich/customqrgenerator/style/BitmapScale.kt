package com.github.alexzhirkevich.customqrgenerator.style

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap

/**
 * Way of getting [Bitmap] from [Drawable]
 * */
fun interface BitmapScale {

    /**
     * Create [Bitmap] from [Drawable] with desired [width] and [height]
     * in a specific way
     * */
    fun scale(drawable: Drawable, width: Int, height: Int): Bitmap

    /**
     * Resize given image. Image's aspect ratio can be broken
     * */
    
    object FitXY : BitmapScale {
        override fun scale(drawable: Drawable, width: Int, height: Int): Bitmap {
            return drawable.toBitmap(
                width, height,
                config = Bitmap.Config.ARGB_8888
            )
        }
    }

    /**
     * Crop given image and cut necessary bitmap from center. Image's aspect ratio will be kept.
     * */
    
    object CenterCrop : BitmapScale {
        override fun scale(drawable: Drawable, width: Int, height: Int): Bitmap {
            var iWidth = drawable.intrinsicWidth
            var iHeight = drawable.intrinsicHeight

            if (iWidth == -1 || iHeight == -1 ||
                width / height.toDouble() == iWidth / iHeight.toDouble()
            )
                return drawable.toBitmap(
                    width, height,
                    config = Bitmap.Config.ARGB_8888
                )

            if (iWidth != width || iHeight != height) {
                val scale = maxOf(
                    width.toDouble() / iWidth,
                    height.toDouble() / iHeight
                )

                iWidth = (iWidth * scale).toInt() + 1
                iHeight = (iHeight * scale).toInt() + 1
            }

            val bitmap = drawable.toBitmap(
                iWidth, iHeight,
                config = Bitmap.Config.ARGB_8888
            )
            val x = (iWidth - width) / 2
            val y = (iHeight - height) / 2

            val newBmp = Bitmap.createBitmap(bitmap, x, y, width, height)
            if (newBmp !== bitmap)
                bitmap.recycle()

            return newBmp
        }
    }
}

