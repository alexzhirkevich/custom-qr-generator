package com.github.alexzhirkevich.customqrgenerator.style

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Way of getting [Bitmap] from [Drawable]
 * */
sealed interface BitmapScale {

    /**
     * Create [Bitmap] from [Drawable] with desired [width] and [height]
     * in a specific way
     * */
    fun scale(drawable: Drawable, width : Int, height : Int) : Bitmap

    /**
     * Resize given image. Image's aspect ratio can be broken
     * */
    @Serializable
    @SerialName("FitCenter")
    object FitCenter : BitmapScale {
        override fun scale(drawable: Drawable, width: Int, height: Int): Bitmap {
            return drawable.toBitmap(width,height,
                config = Bitmap.Config.ARGB_8888)
        }
    }

    /**
     * Crop given image and cut necessary bitmap from center. Image's aspect ratio
     * will be kept.
     * */
    @Serializable
    @SerialName("CenterCrop")
    object CenterCrop : BitmapScale {
        override fun scale(drawable: Drawable, width: Int, height: Int): Bitmap {
            var iWidth = drawable.intrinsicWidth
            var iHeight = drawable.intrinsicHeight

            if (iWidth == -1 || iHeight == -1 ||
                width / height.toDouble() == iWidth/iHeight.toDouble())
                return drawable.toBitmap(width,height,
                    config = Bitmap.Config.ARGB_8888)

            if (iWidth != width || iHeight != height){
                val scale = maxOf(
                    width.toDouble()/iWidth,
                    height.toDouble()/iHeight
                )

                iWidth = (iWidth * scale).toInt() + 1
                iHeight = (iHeight * scale).toInt() + 1
            }

            val bitmap = drawable.toBitmap(iWidth, iHeight,
                config = Bitmap.Config.ARGB_8888)
            val x = (iWidth - width)/2
            val y = (iHeight - height)/2

            val newBmp = Bitmap.createBitmap(bitmap, x,y, width, height)
            if (newBmp !== bitmap)
                bitmap.recycle()

            return newBmp
        }
    }

    companion object : SerializationProvider {
        override val defaultSerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModule {
                polymorphic(BitmapScale::class) {
                    subclass(FitCenter::class)
                    subclass(CenterCrop::class)
                }
            }
        }
    }
}

