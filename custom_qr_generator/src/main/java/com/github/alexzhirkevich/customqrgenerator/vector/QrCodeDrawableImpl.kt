package com.github.alexzhirkevich.customqrgenerator.vector

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.withTranslation
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.encoder.QrCodeMatrix
import com.github.alexzhirkevich.customqrgenerator.encoder.neighbors
import com.github.alexzhirkevich.customqrgenerator.encoder.toQrMatrix
import com.github.alexzhirkevich.customqrgenerator.style.DrawableSource
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.encoder.Encoder
import kotlinx.coroutines.runBlocking
import java.nio.charset.Charset
import kotlin.math.roundToInt


fun QrCodeDrawable(
    context: Context,
    data: QrData,
    options: QrVectorOptions,
    charset: Charset?=null
) : Drawable = QrCodeDrawableImpl(context, data, options, charset)


/**
 * Vector QR code image
 * */
internal class QrCodeDrawableImpl(
    private val context: Context,
    data: QrData,
    private val options: QrVectorOptions,
    charset: Charset?=null
) : Drawable() {

    private var codeMatrix : QrCodeMatrix = Encoder.encode(
        data.encode(), options.errorCorrectionLevel.lvl, charset?.let {
            mapOf(EncodeHintType.CHARACTER_SET to it)
        })
        .matrix.toQrMatrix()

    private var modifiedMatrix : QrCodeMatrix = codeMatrix
        .copy()

    private var mColorFilter : ColorFilter?=null
    private var mAlpha  = 255

    override fun setAlpha(alpha: Int) {
        mAlpha = alpha
        listOf(darkPixelPaint, lightPixelPaint,ballPaint,framePaint)
            .onEach {
                it.alpha = alpha
            }
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mColorFilter = colorFilter
        listOf(darkPixelPaint, lightPixelPaint,ballPaint,framePaint)
            .onEach {
                it.colorFilter = colorFilter
            }

    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    private var size : Float = 0f

    private var pixelSize : Float = 0f
    private var ballPath : Path = Path()
    private var framePath : Path = Path()

    private var logo : Bitmap?= null

    private val darkPixelPath : Path = Path()
    private val lightPixelPath : Path = Path()

    private var darkPixelPaint : Paint = Paint()
    private var lightPixelPaint: Paint = Paint()
    private var framePaint: Paint = Paint()
    private var ballPaint: Paint = Paint()

    override fun setBounds(bounds: Rect) {
        super.setBounds(bounds)
        resize()
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        resize()
    }

    override fun draw(canvas: Canvas) {

        if (pixelSize < Float.MIN_VALUE)
            return

        val (w,h) = with(bounds){ width() to height() }
        val (offsetX, offsetY) = with(options.offset){ listOf(x,y) }
            .map { it.coerceIn(-1f, 1f) + 1 }

        canvas.withTranslation(
            (w - size)/2f * offsetX,
            (h - size)/2f * offsetY
        ) {

            drawPath(darkPixelPath, darkPixelPaint)
            drawPath(lightPixelPath, lightPixelPaint)

            if (options.colors.ball !is QrVectorColor.Unspecified) {
                listOf(
                    2 to 2,
                    2 to codeMatrix.size - 5,
                    codeMatrix.size - 5 to 2
                ).forEach {
                    withTranslation(
                        it.first * pixelSize,
                        it.second * pixelSize
                    ) {
                        drawPath(ballPath, ballPaint)
                    }
                }
            }

            if (options.colors.frame !is QrVectorColor.Unspecified) {
                listOf(
                    0 to 0,
                    0 to codeMatrix.size - 7,
                    codeMatrix.size - 7 to 0
                ).forEach {
                    withTranslation(
                        it.first * pixelSize,
                        it.second * pixelSize
                    ) {
                        drawPath(framePath, framePaint)
                    }
                }
            }

            val nLogo = logo
            if (nLogo != null){
                val (x,y) = (w - nLogo.width) /2f to (h - nLogo.height) /2f
                drawBitmap(nLogo, x,y, null)
            }
        }
    }

    private fun resize(){
        size = minOf(bounds.width(), bounds.height()) *
                (1 - options.padding.coerceIn(0f,.5f))
        pixelSize = size / codeMatrix.size
        ballPath = options.shapes.ball.createPath(pixelSize * 3f, Neighbors.Empty)
        framePath = options.shapes.frame.createPath(pixelSize * 7f, Neighbors.Empty)

        val singleDarkPixelPath = with(options.shapes.darkPixel) {
            if (isDependOnNeighbors)
                null else createPath(
                pixelSize,
                Neighbors.Empty
            )
        }
        val singleLightPixelPath = with(options.shapes.lightPixel){
            if (isDependOnNeighbors)
                null else createPath(pixelSize, Neighbors.Empty)
        }
        darkPixelPaint = options.colors.dark.createPaint(
                codeMatrix.size * pixelSize,
                codeMatrix.size * pixelSize,
        )
        lightPixelPaint = options.colors.light.createPaint(
                codeMatrix.size * pixelSize,
                codeMatrix.size * pixelSize
        )
        ballPaint = options.colors.ball.createPaint(
                pixelSize * 3f,
                pixelSize * 3f,
        )
        framePaint = options.colors.frame.createPaint(
                pixelSize * 7f,
                pixelSize * 7f,
        )

        colorFilter = mColorFilter
        alpha = mAlpha

        darkPixelPath.reset()
        lightPixelPath.reset()

        modifiedMatrix = codeMatrix.copy()
        logo = if (options.logo.drawable != DrawableSource.Empty) {
            val logoSize = size * options.logo.size
            val logoDrawable = runBlocking {
                options.logo.drawable.get(context)
            }

            val logoSizeInQrPixels = (logoSize * (1+options.logo.padding.value)/pixelSize)
                .roundToInt()
            val start = (codeMatrix.size - logoSizeInQrPixels)/2
            val end = (codeMatrix.size + logoSizeInQrPixels)/2

            for (x in start until end){
                for (y in start until end){
                    kotlin.runCatching {
                        if (options.logo.shape.invoke(
                                (x - start),
                                (y - start),
                                logoSizeInQrPixels,
                                Neighbors.Empty
                            )
                        ) {
                            modifiedMatrix[x, y] = QrCodeMatrix.PixelType.Logo
                        }
                    }
                }
            }

            options.logo.scale.scale(
                logoDrawable, logoSize.roundToInt(), logoSize.roundToInt()
            )
        } else null


        for (x in 0 until codeMatrix.size) {
            for (y in 0 until codeMatrix.size) {

                when {
                    options.colors.frame is QrVectorColor.Unspecified &&
                            x == 0 && y == 0 ||
                            x == 0 && y == codeMatrix.size-7 ||
                            x == codeMatrix.size-7 && y == 0  -> darkPixelPath.addPath(
                        framePath,
                        x.toFloat() * pixelSize,
                        y.toFloat() * pixelSize
                    )

                    options.colors.ball is QrVectorColor.Unspecified &&
                            x == 2 && y == codeMatrix.size - 5 ||
                            x == codeMatrix.size - 5 && y == 2||
                            x == 2 && y == 2 -> {
                        darkPixelPath.addPath(
                            ballPath,
                            x.toFloat() * pixelSize,
                            y.toFloat() * pixelSize
                        )
                    }

                    x in 0..6 && y in 0..6 ||
                            x <7 && y in codeMatrix.size - 7 until codeMatrix.size ||
                            x in codeMatrix.size - 7 until codeMatrix.size && y < 7 -> Unit

                    else -> when(modifiedMatrix[x,y]) {
                        QrCodeMatrix.PixelType.DarkPixel ->
                            darkPixelPath.addPath(
                                singleDarkPixelPath ?:
                                options.shapes.darkPixel.createPath(pixelSize, codeMatrix.neighbors(x,y)),
                                x.toFloat() * pixelSize,y.toFloat() * pixelSize )
                        QrCodeMatrix.PixelType.LightPixel ->
                            lightPixelPath.addPath(singleLightPixelPath ?:
                            options.shapes.lightPixel.createPath(
                                pixelSize, codeMatrix.neighbors(x,y)),
                                x.toFloat() * pixelSize,y.toFloat() * pixelSize )
                    }
                }
            }
        }

    }
}