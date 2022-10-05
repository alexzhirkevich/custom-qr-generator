package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.github.alexzhirkevich.customqrgenerator.style.QrBackground
import com.github.alexzhirkevich.customqrgenerator.style.QrColors
import com.github.alexzhirkevich.customqrgenerator.style.QrLogo

/**
 * Drawable representation of QR code.
 *
 * All changes are reflected in the [drawable] instance.
 * */
interface QrDrawable {

    /**
     * [ImageView.invalidate] should be called when mutations performed. Especially
     * for animations.
     * */
    val drawable : Drawable

    val isRecycled : Boolean

    suspend fun setColors(colors: QrColors)

    suspend fun setLogo(logo: QrLogo)

    suspend fun setBackground(backgroundImage: QrBackground)

    /**
     * Free allocated memory.
     * Does not perform synchronously.
     *
     * This is an advanced call, and normally need not be called,
     * since the normal GC process will free up this memory when
     * there are no more references to this object.
     *
     * @see Bitmap.recycle
     * */
    fun recycle()
}

