package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Bitmap
import androidx.core.graphics.alpha
import androidx.core.graphics.drawable.toBitmap
import com.github.alexzhirkevich.customqrgenerator.encoder.QrCodeMatrix
import com.github.alexzhirkevich.customqrgenerator.encoder.QrEncoder
import com.github.alexzhirkevich.customqrgenerator.encoder.QrRenderResult
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.style.QrColor
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.*
import kotlin.math.roundToInt

class QrGenerator(
    private val threadPolicy: ThreadPolicy = ThreadPolicy
        .SingleThread,
) : QrCodeGenerator {

    enum class ThreadPolicy {

        SingleThread {
            override suspend operator fun invoke(size: Int, block: (IntRange, IntRange) -> Unit) {
                block((0 until size), (0 until size))
            }
        },

        DoubleThread {
            override suspend operator fun invoke(size: Int, block: (IntRange, IntRange) -> Unit) {
                coroutineScope {
                    listOf(
                        (0 until size) to (0 until size / 2),
                        (0 until size) to (size / 2 until size),
                    ).map {
                        launch(Dispatchers.Default) {
                            block(it.first, it.second)
                        }
                    }
                }.joinAll()
            }
        },

        QuadThread {
            override suspend operator fun invoke(size: Int, block: (IntRange, IntRange) -> Unit) {
                coroutineScope {
                    listOf(
                        (0 until size / 2) to (0 until size / 2),
                        (0 until size / 2) to (size / 2 until size),
                        (size / 2 until size) to (0 until size / 2),
                        (size / 2 until size) to (size / 2 until size)
                    ).map {
                        launch(Dispatchers.Default) {
                            block(it.first, it.second)
                        }
                    }
                }.joinAll()
            }
        };

        abstract suspend operator fun invoke(size : Int, block : (IntRange, IntRange) -> Unit)
    }

    override fun generateQrCode(data: QrData, options: QrOptions): Bitmap = runBlocking {
        kotlin.runCatching {
            createQrCodeInternal(data, options)
        }.getOrElse {
            throw QrCodeCreationException(it)
        }
    }
    override suspend fun generateQrCodeSuspend(data: QrData, options: QrOptions): Bitmap =
        withContext(Dispatchers.Default) {
            kotlin.runCatching {
                createQrCodeInternal(data, options)
            }.getOrElse {
                throw if (it is CancellationException)
                     it else QrCodeCreationException(cause = it)
            }
        }

    private suspend fun createQrCodeInternal(
       data : QrData,  options: QrOptions
    ) : Bitmap {

        val encoder = QrEncoder(options.copy(errorCorrectionLevel = options.actualEcl))
        val result = encoder.encode(data.encode())

        val bmp = Bitmap.createBitmap(
            options.size, options.size,
            Bitmap.Config.ARGB_8888
        )

        return bmp.apply {
            drawCode(result, options)
        }
    }

    private suspend fun Bitmap.drawCode(result: QrRenderResult, options: QrOptions) = coroutineScope{
        with(result) {


            val bgBitmap = withContext(Dispatchers.IO) {
                options.background?.drawable
                    ?.toBitmap(width, height, Bitmap.Config.ARGB_8888)
            }

            val bgBitmapPixels = if (bgBitmap != null)
                IntArray(width * height) else null

            bgBitmap?.getPixels(bgBitmapPixels, 0,width,0,0,width, height)
            bgBitmap?.recycle()

            val array = IntArray(width*height)

            threadPolicy.invoke(width){ xrange, yrange ->

                for (x in xrange) {
                    for (y in yrange) {
                        val bitmapBgColor = options.colors.background.invoke(
                            x,y, bitMatrix.size, pixelSize
                        )
                        val bgColor =  bgBitmapPixels?.get(x + y * width)?.takeIf { it.alpha > 0 }
                            ?.let { QrUtil.mixColors(it, bitmapBgColor, options.background?.alpha ?: 0f) }
                            ?: bitmapBgColor

                        array[x + y * width] =bgColor
                    }
                }
                for (x in xrange) {
                    for (y in yrange) {
                        ensureActive()

                        val inCodeRange = x in padding until width -padding - error &&
                                y in padding until height-padding - error && options.shapes.hightlighting
                            .invoke(
                                x - padding,
                                y - padding,
                                bitMatrix.size - error,
                                Neighbors.Empty
                            )

                        if (inCodeRange){
                            val pixel = bitMatrix[x - padding, y - padding]

                            val realX = minOf(x - padding, width - x - error - padding)
                            val realY = minOf(y - padding, height  - y - error - padding)

                            val emptyCorner = width - x  < x && height - y < y

                            val bottom = height - y < y

                            val color = when {
                                pixel == QrCodeMatrix.PixelType.DarkPixel &&
                                        !emptyCorner && options.colors.ball !is QrColor.Unspecified &&
                                        ball.let {
                                            realX in it.x until it.x + it.size  &&
                                                    realY in it.y until it.y + it.size
                                        } -> options.colors.ball.invoke(
                                    i = realX - ball.x,
                                    j= (realY-ball.y).let {
                                       if (bottom) ball.size - it else it
                                    },
                                    elementSize = ball.size,
                                    qrPixelSize = pixelSize)

                                pixel == QrCodeMatrix.PixelType.DarkPixel &&
                                        !emptyCorner && options.colors.frame !is QrColor.Unspecified &&
                                        frame.let {
                                            realX in it.x until it.x + it.size &&
                                                    realY in it.y until it.y + it.size
                                        } -> options.colors.frame.invoke(
                                    i = realX-frame.x,
                                    j = (realY-frame.y).let {
                                        if (bottom) frame.size - it else it
                                    },
                                    elementSize = frame.size,
                                    qrPixelSize =  pixelSize)

                                pixel == QrCodeMatrix.PixelType.DarkPixel  && options.colors.dark.invoke(
                                    x-padding, y-padding, bitMatrix.size, pixelSize
                                ).alpha > 0 -> options.colors.dark.invoke(
                                    x-padding, y-padding, bitMatrix.size, pixelSize
                                )
                                pixel == QrCodeMatrix.PixelType.LightPixel && options.colors.light.invoke(
                                    x-padding, y-padding, bitMatrix.size, pixelSize
                                ).alpha > 0 -> options.colors.light.invoke(
                                    x-padding, y-padding, bitMatrix.size, pixelSize
                                )
                                else -> {
                                    val bgColor = array[x+error/2 + (y+error/2) * width]

                                    val codeBg = options.colors.highlighting.invoke(
                                        x-padding, y-padding, bitMatrix.size - padding*2, pixelSize
                                    )

                                    if (codeBg.alpha >0)
                                        QrUtil.mixColors(codeBg, bgColor, codeBg.alpha /255f)
                                    else bgColor
                                }
                            }

                            array[x+error/2 + (y+error/2) * width] = color
                        }
                    }
                }
            }

            if (options.logo != null) kotlin.run {
                val logoSize = ((width - padding*2) /
                        options.codeShape.shapeSizeIncrease *
                        options.logo.size)
                    .roundToInt()

                val bitmapLogo = options.logo.scale.scale(options.logo.drawable, logoSize, logoSize)
                val logoPixels = IntArray(logoSize*logoSize)
                bitmapLogo.getPixels(logoPixels,0, logoSize, 0,0, logoSize, logoSize)
                bitmapLogo.recycle()

                val logoTopLeft = (width - logoSize) / 2


                for (i in 0 until logoSize) {
                    for (j in 0 until logoSize) {

                        ensureActive()

                        if (!options.logo.shape.invoke(
                                i, j, logoSize,
                                Neighbors.Empty
                            )
                        ) {
                            continue
                        }

                        if (logoPixels[i + j * logoSize].alpha > 0) {
                            runCatching {
                                array[logoTopLeft + i + (logoTopLeft + j) * width] =
                                    logoPixels[i + j * logoSize]
                            }
                        }
                    }
                }
            }

            setPixels(array,0,width,0,0,width,height)
        }
    }

}

internal val QrOptions.actualEcl : QrErrorCorrectionLevel
    get() = if (errorCorrectionLevel == QrErrorCorrectionLevel.Auto) when {
        logo == null -> errorCorrectionLevel
        logo.size * (1 + logo.padding.value) > .3 ->
            QrErrorCorrectionLevel.High
        logo.size * (1 + logo.padding.value) in .2 .. .3
                && errorCorrectionLevel.lvl < ErrorCorrectionLevel.Q ->
            QrErrorCorrectionLevel.MediumHigh
        errorCorrectionLevel.lvl < ErrorCorrectionLevel.M ->
            QrErrorCorrectionLevel.Medium
        else -> errorCorrectionLevel
    } else errorCorrectionLevel