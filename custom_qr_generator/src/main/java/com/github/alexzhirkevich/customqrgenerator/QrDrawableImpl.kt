package com.github.alexzhirkevich.customqrgenerator

import android.content.Context
import android.graphics.*
import android.graphics.drawable.*
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.convertTo
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.github.alexzhirkevich.customqrgenerator.style.*
import kotlinx.coroutines.*
import kotlin.math.roundToInt

class QrDrawableImpl(
    private val context: Context,
    private val options: QrOptions,
    private val darkPixelsBitmap: Bitmap,
    private val lightPixelsBitmap : Bitmap,
) : QrDrawable{

    override val drawable: Drawable
        get() = if (isRecycled) EmptyDrawable else layerDrawable!!

    override var isRecycled = false
        private set


    private var layerDrawable : LayerDrawable? =
        ContextCompat.getDrawable(context,R.drawable.qrcode)
                as LayerDrawable

    private val resources = context.resources

    private val codeSize = with(darkPixelsBitmap){
        if (width < height)
            width.toFloat()/options.width
        else height.toFloat()/options.height
    }

    private var lastImage : QrBackground?=null
    private var lastLogo : QrLogo?=null
    private var lastColors : QrColors ?=null

    override suspend fun setColors(colors: QrColors) {
        if (isRecycled)
            return

        setColorsInternal(colors)
    }

    override suspend fun setLogo(logo: QrLogo) {
        if (lastLogo == logo || isRecycled)
            return

        setLogoInternal(logo)
    }

    override suspend fun setBackground(image: QrBackground) {
        if (lastImage == image || isRecycled) return

        setBackgroundInternal(image)
    }

    override fun recycle() {
        if (isRecycled)
            return
        isRecycled = true

        lastColors = null
        lastLogo = null
        lastImage = null

        layerDrawable = null
    }

    internal suspend fun setColorsInternal(colors: QrColors) {
//        coroutineScope {
            if (lastColors?.dark != colors.dark) {
//                launch {
                    setDarkPixelColor(colors.dark)
//                }
            }
            if (lastColors?.light != colors.light) {
//                launch {
                    setLightPixelColor(colors.light)
//                }
            }
            if (lastColors?.highlighting != colors.highlighting) {
//                launch {
                    setHighlightingColor(colors.highlighting)
//                }
            }
//        }
        lastColors = colors
    }

    internal suspend fun setLogoInternal(logo: QrLogo) {
        val logoSize = ((1 - options.padding) /
                options.codeShape.shapeSizeIncrease *
                logo.size)

        val logoDrawable = options.logo.drawable
            .get(context)
            .takeIf { it !is EmptyDrawable }
            ?.let {
                val width = (logoSize * options.width).roundToInt()
                val height = (logoSize * options.height).roundToInt()
                val size = minOf(width,height)
                options.logo.scale.scale(it, size, size)
                    .extendSquareToRect(width, height)
            }
            ?.toDrawable(resources)
            ?.let {
                insetDrawable(it, logoSize)
            }
        layerDrawable?.setDrawableByLayerId(R.id.qr_logo, logoDrawable)
        lastLogo = logo
    }

    internal suspend fun setBackgroundInternal(image: QrBackground) {
        val bgDrawable = image.drawable.get(context)
            .takeIf { it !is EmptyDrawable }
            ?.let {
                image.scale.scale(it, options.width, options.height)
            }
            ?.toDrawable(resources)
            ?.apply {
                alpha = (255 * image.alpha.coerceIn(0f, 1f))
                    .roundToInt()
            }
        layerDrawable?.setDrawableByLayerId(R.id.qr_background_image, bgDrawable)
        lastImage = image
    }

    private fun insetDrawable(
        drawable: Drawable, elementSize : Float
    ) : Drawable =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val insetHorizontal = (1 - elementSize)/2 * (1 + options.offset.x)
            val insetVertical = (1 - elementSize)/2 * (1 + options.offset.y)

            InsetDrawable(
                drawable,
                (1 - elementSize)/2 * (1 + options.offset.x) ,
                (1 - elementSize)/2 * (1 + options.offset.y) ,
                (1 - elementSize)/2 * (1 - options.offset.x),
                (1 - elementSize)/2 * (1 - options.offset.y)
            )
        } else object : Drawable() {

            override fun draw(canvas: Canvas) {
                val width = (elementSize * bounds.width()).roundToInt()
                val height = (elementSize * bounds.height()).roundToInt()

                val bmp = drawable.toBitmap(
                    width, height, Bitmap.Config.ARGB_8888
                )

                canvas.drawBitmap(
                    bmp,
                    (bounds.width() - width) / 2f * (1 + options.offset.x.coerceIn(-1f,1f)),
                    (bounds.height() - height) / 2f * (1 + options.offset.y.coerceIn(-1f,1f)),
                    null
                )

            }

            override fun setAlpha(alpha: Int) {}

            override fun setColorFilter(colorFilter: ColorFilter?) {}

            override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
        }

    private fun drawableForColor(width : Int, height : Int, color : QrColor) : Drawable {
        return when(color){
            is QrColor.Solid -> ColorDrawable(color.color)
            is QrColor.LinearGradient -> GradientDrawable(
                when(color.orientation){
                    QrColor.LinearGradient.Orientation.Horizontal ->
                        GradientDrawable.Orientation.LEFT_RIGHT
                    QrColor.LinearGradient.Orientation.Vertical ->
                        GradientDrawable.Orientation.TOP_BOTTOM
                    QrColor.LinearGradient.Orientation.LeftDiagonal ->
                        GradientDrawable.Orientation.TL_BR
                    QrColor.LinearGradient.Orientation.RightDiagonal ->
                        GradientDrawable.Orientation.BL_TR
                },
                intArrayOf(color.startColor, color.endColor)
            )
            is QrColor.RadialGradient ->  GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(color.startColor,color.endColor)
            ).apply {
                gradientType = GradientDrawable.RADIAL_GRADIENT
                val width = (layerDrawable?.bounds?.width() ?: width)/2 *
                        color.radius
                val height = (layerDrawable?.bounds?.height() ?: height)/2 *
                        color.radius
                gradientRadius = maxOf(width,height)
            }
            else -> createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                val pixels = IntArray(width*height)
                for(i in 0 until width){
                    for (j in 0 until height){
                        pixels[i + width*j] = color.invoke(i,j, width,height)
                    }
                }
                setPixels(pixels, 0, width, 0,0, width, height)
            }.toDrawable(resources)
        }
    }

    private fun setBgColor(color: QrColor) {
        layerDrawable?.setDrawableByLayerId(R.id.qr_background_color,
            drawableForColor(options.width,options.height, color))
    }

    private fun setDarkPixelColor(color : QrColor) {

        val colorDrawable = drawableForColor(
            darkPixelsBitmap.width,darkPixelsBitmap.height, color
        )

        val d = createBitmap(
            darkPixelsBitmap.width,
            darkPixelsBitmap.height,
            Bitmap.Config.ARGB_8888
        ).applyCanvas {
            colorDrawable.setBounds(0,0, width,height)
            colorDrawable.draw(this)
            drawBitmap(darkPixelsBitmap, 0f,0f, Paint().apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            })
        }.toDrawable(resources)
            .let {
                insetDrawable(it, codeSize)
            }

        layerDrawable?.setDrawableByLayerId(R.id.qr_dark_pixels,d)
    }

    private fun setLightPixelColor(color : QrColor) {

        val colorDrawable = drawableForColor(
            lightPixelsBitmap.width, lightPixelsBitmap.height, color
        )

        val d =  createBitmap(
            lightPixelsBitmap.width,
            lightPixelsBitmap.height,
            Bitmap.Config.ARGB_8888
        ).applyCanvas {
            colorDrawable.setBounds(0,0, width,height)
            colorDrawable.draw(this)
            drawBitmap(lightPixelsBitmap, 0f,0f, Paint().apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            })
        }.toDrawable(resources)
            .let {
                insetDrawable(it, codeSize)
            }

        layerDrawable?.setDrawableByLayerId(R.id.qr_light_pixels,d)
    }

    private fun setHighlightingColor(color : QrColor) {
        val size = (minOf(options.width,options.height) * codeSize).roundToInt()

        val colorDrawable = insetDrawable(
            drawable = drawableForColor(size, size, color),
            elementSize = codeSize
        ).apply {
            setBounds(0,0,size,size)
        }
        layerDrawable?.setDrawableByLayerId(R.id.qr_highlighting, colorDrawable)

    }
}

private fun Bitmap.extendSquareToRect(width: Int, height: Int) : Bitmap {
    assert(this.width == this.height)
    assert(this.width == width || this.height == height)

    val pixels = IntArray(this.width * this.height)
    getPixels(pixels, 0 ,this.width, 0,0, this.width, this.height)

    return createBitmap(width, height).apply {
        setPixels(pixels, 0, this.width,
            (width - this.width) / 2, (height - this.height) / 2,
            this@extendSquareToRect.width, this@extendSquareToRect.height
        )
    }
}
