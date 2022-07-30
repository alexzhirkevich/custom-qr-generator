package com.github.alexzhirkevich.customqrgenerator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.core.graphics.*
import androidx.core.graphics.drawable.toBitmap
import com.github.alexzhirkevich.customqrgenerator.encoder.QrEncoder
import com.github.alexzhirkevich.customqrgenerator.encoder.QrRenderResult
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.style.QrColor
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.*
import kotlin.math.roundToInt


class QrGenerator() : QrCodeGenerator {

    override fun generateQrCode(data: QrData, options: QrOptions): Bitmap =
        kotlin.runCatching {
            val encoder = QrEncoder(options.copy(errorCorrectionLevel = options.actualEcl))

            val result = encoder.encode(data.encode())
            createQrCodeInternal(result,options) { true }
        }.getOrElse {
            throw QrCodeCreationException(it)
        }

    override suspend fun generateQrCodeSuspend(data: QrData, options: QrOptions): Bitmap =
        withContext(Dispatchers.Main) {
            kotlin.runCatching {
                val encoder = QrEncoder(options.copy(errorCorrectionLevel = options.actualEcl))
                val startTime = System.currentTimeMillis()
                val result = encoder.encodeSuspend(data.encode())
                coroutineScope {
                    createQrCodeInternal(result, options, coroutineContext::isActive)
                }
            }.getOrElse {
                if (it is CancellationException)
                    throw it
                else throw QrCodeCreationException(cause = it)
            }
        }

    private fun createQrCodeInternal(
       result: QrRenderResult, options: QrOptions, isActive : () -> Boolean
    ) : Bitmap {

        val bmp = Bitmap.createBitmap(
            options.size, options.size,
            Bitmap.Config.ARGB_8888
        )

        return bmp.apply {
            drawCode(result, options, isActive)
            drawLogo(result, options, isActive)
        }
    }

    private fun Bitmap.drawCode(result: QrRenderResult, options: QrOptions, isActive: () -> Boolean) {
        with(result) {
            val bgBitmap = options.background?.drawable
                ?.toBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (!isActive()) {
                        throw CancellationException()
                    }

                    val inCodeRange = x in padding until width -padding &&
                            y in padding until height-padding && options.shapes.background
                        .invoke(x - padding, y - padding, bitMatrix.width, pixelSize, Neighbors.Empty)


                    if (inCodeRange){
                        val pixel = bitMatrix[x - padding, y - padding].toInt()

                        val realX = minOf(x - padding, bitMatrix.width - x - error - padding)
                        val realY = minOf(y - padding, bitMatrix.height - y - error - padding)

                        val topRightCorner = bitMatrix.width - x  < x && bitMatrix.height - y < y

                        val color = when {
                            pixel == 1 && !topRightCorner && options.colors.ball !is QrColor.Unspecified &&
                                    ball.let {
                                        realX in it.x until it.x + it.size  &&
                                                realY in it.y until it.y + it.size
                                    } -> options.colors.ball.invoke(
                                realX - ball.x, realY-ball.y, ball.size, pixelSize)

                            pixel == 1 && !topRightCorner && options.colors.frame !is QrColor.Unspecified &&
                                    frame.let {
                                        realX in it.x until it.x + it.size &&
                                                realY in it.y until it.y + it.size
                                    } -> options.colors.frame.invoke(
                                realX-frame.x, realY-frame.y, frame.size, pixelSize)

                            pixel == 1  && options.colors.dark.invoke(
                                x, y, bitMatrix.width, pixelSize
                            ).alpha > 0 -> options.colors.dark.invoke(
                                x, y, bitMatrix.width, pixelSize
                            )
                            pixel == 0 && options.colors.light.invoke(
                                x, y, bitMatrix.width, pixelSize
                            ).alpha > 0 -> options.colors.light.invoke(
                                x, y, bitMatrix.width, pixelSize
                            )
                            else -> {
                                val bgColor = bgBitmap?.get(x,y)?.takeIf { it.alpha > 0 } ?:
                                options.colors.bitmapBackground.invoke(
                                    x,y, bitMatrix.width, pixelSize
                                )
                                val codeBg = options.colors.codeBackground.invoke(
                                    x-padding, y-padding, bitMatrix.width - padding*2, pixelSize
                                )

                                if (codeBg.alpha >0)
                                    QrUtil.mixColors(codeBg, bgColor, codeBg.alpha /255f)
                                else bgColor
                            }
                        }
                        this@drawCode[x+error/2, y+error/2] = color
                    } else {
                        this@drawCode[x,y] = bgBitmap?.get(x,y)?.takeIf { it.alpha > 0 } ?:
                        options.colors.bitmapBackground.invoke(
                            x,y, bitMatrix.width, pixelSize
                        )
                    }
                }
            }
            bgBitmap?.recycle()
        }
    }

    private fun Bitmap.drawLogo(
        renderResult: QrRenderResult, options: QrOptions, isActive: () -> Boolean
    ) = with(renderResult){
        if (options.logo != null) {
            val logoSize = ((width - shapeIncrease * 4) * options.logo.size).roundToInt()
            val bitmapLogo = options.logo.drawable
                .toBitmap(logoSize, logoSize, Bitmap.Config.ARGB_8888)
            val logoTopLeft = (width - logoSize) / 2

            for (i in 0 until bitmapLogo.width) {
                for (j in 0 until bitmapLogo.height) {

                    if (!options.logo.shape.invoke(i,j,bitmapLogo.width, pixelSize, Neighbors.Empty)){
                        continue
                    }
                    if (!isActive()) {
                        throw CancellationException()
                    }

                    if (bitmapLogo[i, j] != Color.TRANSPARENT) {
                        runCatching {
                            this@drawLogo[logoTopLeft + i , logoTopLeft + j] =
                                bitmapLogo[i, j]
                        }
                    }
                }
            }
            bitmapLogo.recycle()
        }
    }
}

internal val QrOptions.actualEcl : QrErrorCorrectionLevel
    get() = if (errorCorrectionLevel == QrErrorCorrectionLevel.Auto) when {
        logo == null -> errorCorrectionLevel
        logo.size * (1 + logo.padding) > .3 ->
            QrErrorCorrectionLevel.High
        logo.size * (1 + logo.padding) in .2 .. .3
                && errorCorrectionLevel.lvl < ErrorCorrectionLevel.Q ->
            QrErrorCorrectionLevel.MediumHigh
        errorCorrectionLevel.lvl < ErrorCorrectionLevel.M ->
            QrErrorCorrectionLevel.Medium
        else -> errorCorrectionLevel
    } else errorCorrectionLevel