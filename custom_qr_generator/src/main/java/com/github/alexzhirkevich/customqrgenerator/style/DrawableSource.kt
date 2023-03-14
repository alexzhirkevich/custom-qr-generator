

package com.github.alexzhirkevich.customqrgenerator.style

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri

//fun interface DrawableSource {
//
//    fun get(context: Context) : Drawable
//
//
//    object Empty : DrawableSource {
//        override fun get(context: Context): Drawable = EmptyDrawable
//    }
//
//    /**
//     * Load image from resources.
//     * */
//
//    data class Resource(@DrawableRes val id : Int) : DrawableSource {
//        override fun get(context: Context): Drawable =
//            requireNotNull(ContextCompat.getDrawable(context, id))
//    }
//
//    /**
//     * Load image from file system. App must have permission to read this file
//     * */
//
//    @Suppress("deprecation")
//    data class File(val uri : String) : DrawableSource {
//
//        override fun get(context: Context): Drawable =
//            if (Build.VERSION.SDK_INT < 28)
//                MediaStore.Images.Media
//                    .getBitmap(context.contentResolver, uri.toUri())
//                    .copy(Bitmap.Config.ARGB_8888, false)
//                    .toDrawable(context.resources)
//            else ImageDecoder
//                .decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri.toUri()))
//                .copy(Bitmap.Config.ARGB_8888, false)
//                .toDrawable(context.resources)
//    }
//
//    class Custom(val drawable: Drawable) : DrawableSource {
//        override fun get(context: Context): Drawable = drawable
//    }
//}

internal object EmptyDrawable : Drawable() {
    override fun draw(canvas: Canvas) = Unit
    override fun setAlpha(alpha: Int)  = Unit
    override fun setColorFilter(colorFilter: ColorFilter?) = Unit
    override fun getOpacity(): Int = PixelFormat.TRANSPARENT
}