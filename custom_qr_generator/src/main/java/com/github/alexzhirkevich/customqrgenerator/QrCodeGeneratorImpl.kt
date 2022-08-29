package com.github.alexzhirkevich.customqrgenerator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.alpha
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import com.github.alexzhirkevich.customqrgenerator.encoder.QrCodeMatrix
import com.github.alexzhirkevich.customqrgenerator.encoder.QrEncoder
import com.github.alexzhirkevich.customqrgenerator.encoder.QrRenderResult
import com.github.alexzhirkevich.customqrgenerator.style.EmptyDrawable
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.style.QrColor
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.*
import kotlin.math.roundToInt

internal class QrCodeGeneratorImpl(
    private val context: Context,
    private val threadPolicy: ThreadPolicy
) : QrCodeGenerator {

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
            options.width, options.height,
            Bitmap.Config.ARGB_8888
        )

        return bmp.apply {
            drawCode(result, options)
        }
    }

    private suspend fun Bitmap.drawCode(
        result: QrRenderResult,
        options: QrOptions,
        drawBg: Boolean = true,
        drawLogo : Boolean = true
    ) = coroutineScope{
        with(result) {

            val bgBitmap = options.background.drawable.get(context)
                    .takeIf { it !is EmptyDrawable && drawBg }
                    ?.toBitmap(width, height, Bitmap.Config.ARGB_8888)

            val bgBitmapPixels = if (bgBitmap != null)
                IntArray(width * height) else null

            bgBitmap?.getPixels(bgBitmapPixels, 0,width,0,0,width, height)

            val offsetX = (paddingX * (1+ options.offset.x.coerceIn(-1f,1f))).roundToInt()
            val offsetY = (paddingY * (1+ options.offset.y.coerceIn(-1f,1f))).roundToInt()
            val array = IntArray(width*height)

            threadPolicy.invoke(width, height){ xrange, yrange ->

                if (drawBg){
                    for (x in xrange) {
                        for (y in yrange) {
                            val bitmapBgColor = options.background.color.invoke(
                                x, y,width, height
                            )
                            val bgColor =  bgBitmapPixels?.get(x + y * width)?.takeIf { it.alpha > 0 }
                                ?.let { QrUtil.mixColors(it, bitmapBgColor, it.alpha/255f * options.background.alpha) }
                                ?: bitmapBgColor

                            array[x + y * width] =bgColor
                        }
                    }
                }
                for (x in xrange) {
                    for (y in yrange) {
                        ensureActive()

                        val inCodeRange = x in paddingX until width - paddingX - error &&
                                y in paddingY until height - paddingY - error && options.shapes.highlighting
                            .invoke(
                                x - paddingX,
                                y - paddingY,
                                width -  2 * minOf(paddingX, paddingY),
                                Neighbors.Empty
                            )

                        if (inCodeRange){
                            val pixel = bitMatrix[x - paddingX, y - paddingY]

                            val realX = minOf(x - paddingX, width - x - error - paddingX)
                            val realY = minOf(y - paddingY, height  - y - error - paddingY)

                            val emptyCorner = width - x  < x && height - y < y

                            val bottom = height - y < y
                            val right = height - x < x

                            val color = when {
                                pixel == QrCodeMatrix.PixelType.DarkPixel &&
                                        !emptyCorner && options.colors.ball !is QrColor.Unspecified &&
                                        ball.let {
                                            realX in it.x until it.x + it.size  &&
                                                    realY in it.y until it.y + it.size
                                        } -> options.colors.ball.invoke(
                                    i = (realX - ball.x).let {
                                         if (right && !options.colors.symmetry)
                                             ball.size - it else it
                                    },
                                    j= (realY-ball.y).let {
                                       if (bottom && !options.colors.symmetry)
                                           ball.size - it else it
                                    },
                                    width = ball.size,
                                    height = ball.size
                                )

                                pixel == QrCodeMatrix.PixelType.DarkPixel &&
                                        !emptyCorner && options.colors.frame !is QrColor.Unspecified &&
                                        frame.let {
                                            realX in it.x until it.x + it.size &&
                                                    realY in it.y until it.y + it.size
                                        } -> options.colors.frame.invoke(
                                    i = (realX-frame.x).let{
                                        if (right && !options.colors.symmetry)
                                            frame.size - it else it
                                    },
                                    j = (realY-frame.y).let {
                                        if (bottom && !options.colors.symmetry)
                                            frame.size - it else it
                                    },
                                    width = frame.size,
                                    height = frame.size,
                                )

                                pixel == QrCodeMatrix.PixelType.DarkPixel  && options.colors.dark.invoke(
                                    x-paddingX, y-paddingY, width - 2* paddingX,height - 2* paddingY
                                ).alpha > 0 -> options.colors.dark.invoke(
                                    x-paddingX, y-paddingY, width - 2 * paddingX, height - 2* paddingY
                                )
                                pixel == QrCodeMatrix.PixelType.LightPixel && options.colors.light.invoke(
                                    x-paddingX, y-paddingY, width - 2 * paddingX,height - 2* paddingY
                                ).alpha > 0 -> options.colors.light.invoke(
                                    x-paddingX, y-paddingY, width - 2 * paddingX, height - 2* paddingY
                                )
                                else -> {
                                    val bgColor = array[x+error/2- paddingX + offsetX +
                                            (y+error/2- paddingY + offsetY) * width]

                                    val codeBg = options.colors.highlighting.invoke(
                                        x-paddingX, y-paddingY, width - 2* paddingX,height - 2* paddingY
                                    )

                                    if (codeBg.alpha >0)
                                        QrUtil.mixColors(codeBg, bgColor, codeBg.alpha /255f)
                                    else bgColor
                                }
                            }

                            array[x+error/2 - paddingX + offsetX +
                                    (y+error/2 - paddingY + offsetY) * width] = color
                        }
                    }
                }
            }

            val logoDrawable = options.logo.drawable.get(context)
            if (drawLogo && logoDrawable !is EmptyDrawable) kotlin.run {
                val logoSize = ((width - minOf(paddingX,paddingY)*2) /
                        options.codeShape.shapeSizeIncrease *
                        options.logo.size)
                    .roundToInt()

                val bitmapLogo = options.logo.scale
                    .scale(logoDrawable, logoSize, logoSize)
                val logoPixels = IntArray(logoSize*logoSize)
                bitmapLogo.getPixels(logoPixels,0, logoSize, 0,0, logoSize, logoSize)

                val logoLeft = (width - logoSize) / 2 - paddingX + offsetX
                val logoTop = (height - logoSize) / 2 - paddingY + offsetY

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

                        val bgColorPos = logoLeft + i + (logoTop + j) * width
                        val logoPixel = logoPixels[i + j * logoSize]
                        val logoBgColor = options.logo.backgroundColor.let {
                            if (it is QrColor.Unspecified) array[bgColorPos]
                            else it.invoke(i, j, logoSize, logoSize)
                        }
                        runCatching {

                            array[bgColorPos] = QrUtil.mixColors(
                                logoPixel, logoBgColor, logoPixel.alpha / 255f
                            )
                        }
                    }
                }
            }

            setPixels(array,0,width,0,0,width,height)
        }
    }
}


private val QrOptions.actualEcl : QrErrorCorrectionLevel
    get() = if (errorCorrectionLevel == QrErrorCorrectionLevel.Auto) when {
        logo.size * (1 + logo.padding.value) > .3 ->
            QrErrorCorrectionLevel.High
        logo.size * (1 + logo.padding.value) in .2 .. .3
                && errorCorrectionLevel.lvl < ErrorCorrectionLevel.Q ->
            QrErrorCorrectionLevel.MediumHigh
        errorCorrectionLevel.lvl < ErrorCorrectionLevel.M ->
            QrErrorCorrectionLevel.Medium
        else -> errorCorrectionLevel
    } else errorCorrectionLevel