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
import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

fun interface DrawableSource {

    suspend fun get(context: Context) : Drawable

    @Serializable
    @SerialName("Empty")
    object Empty : DrawableSource {
        override suspend fun get(context: Context): Drawable = EmptyDrawable
    }

    /**
     * Load image from resources.
     * */
    @Serializable
    @SerialName("Resource")
    data class Resource(@DrawableRes val id : Int) : DrawableSource {
        override suspend fun get(context: Context): Drawable =
            requireNotNull(ContextCompat.getDrawable(context, id))
    }

    /**
     * Load image from file system. App must have permission to read this file
     * */
    @Serializable
    @SerialName("File")
    @Suppress("BlockingMethodInNonBlockingContext")
    data class File(val uri : String) : DrawableSource {

        override suspend fun get(context: Context): Drawable =
            withContext(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT < 28)
                    MediaStore.Images.Media
                        .getBitmap(context.contentResolver, uri.toUri())
                        .copy(Bitmap.Config.ARGB_8888,false)
                        .toDrawable(context.resources)
                else ImageDecoder
                    .decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri.toUri()))
                    .copy(Bitmap.Config.ARGB_8888,false)
                    .toDrawable(context.resources)
            }
    }


    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        @Suppress("unchecked_cast")
        override val defaultSerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModule {
                polymorphicDefaultSerializer(DrawableSource::class){
                    Empty.serializer() as SerializationStrategy<DrawableSource>
                }
                polymorphicDefaultDeserializer(DrawableSource::class){
                    Empty.serializer()
                }
                polymorphic(DrawableSource::class) {
                    subclass(Empty::class)
                    subclass(Resource::class)
                    subclass(File::class)
                }
            }
        }
    }
}

internal object EmptyDrawable : Drawable() {
    override fun draw(canvas: Canvas) = Unit
    override fun setAlpha(alpha: Int)  = Unit
    override fun setColorFilter(colorFilter: ColorFilter?) = Unit
    override fun getOpacity(): Int = PixelFormat.TRANSPARENT
}