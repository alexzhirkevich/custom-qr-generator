package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.*
import androidx.core.graphics.drawable.toBitmap
import com.github.alexzhirkevich.customqrgenerator.encoder.QrEncoder
import com.github.alexzhirkevich.customqrgenerator.encoder.QrRenderResult
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.style.QrColor
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.*
import kotlin.math.roundToInt


class QrGenerator : QrCodeGenerator {

    override fun generateQrCode(text: String, options: QrOptions): Bitmap =
        kotlin.runCatching {
            createQrCodeInternal(text,options) { true }
        }.getOrElse {
            throw QrCodeCreationException(it)
        }

    override suspend fun generateQrCodeSuspend(text: String, options: QrOptions): Bitmap =
        withContext(Dispatchers.Default) {
            kotlin.runCatching {
                coroutineScope {
                    createQrCodeInternal(text, options, coroutineContext::isActive)
                }
            }.getOrElse {
                if (it is CancellationException)
                    throw it
                else throw QrCodeCreationException(cause = it)
            }
        }

    private fun createQrCodeInternal(
        text: String, options: QrOptions, isActive : () -> Boolean
    ) : Bitmap {

        val encoder = QrEncoder(options.copy(errorCorrectionLevel = options.actualEcl))
        val result = encoder.encode(text)

        val bmp = Bitmap.createBitmap(
            options.size, options.size,
            Bitmap.Config.ARGB_8888
        )

        return bmp.apply {
            drawBackground(result, options, isActive)
            drawQr(result, options, isActive)
            drawLogo(result, options, isActive)
        }
    }

    private fun Bitmap.drawBackground(
        renderResult: QrRenderResult, options: QrOptions, isActive : () -> Boolean
    ) = with(renderResult){
        if (options.background != null) {
            val backgroundBitmap = options.background.drawable
                .toBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (!isActive()){
                        throw CancellationException()
                    }
                    val color = backgroundBitmap[x, y].toColorLong()

                    val bgImageColor = Color.argb(
                        (color.alpha * options.background.alpha * 255).roundToInt(),
                        (color.red * 255).roundToInt(),
                        (color.green * 255).roundToInt(),
                        (color.blue * 255).roundToInt())

                    val bgColor = options.colors.bitmapBackground.invoke(
                        x, y, options.size, pixelSize)

                    kotlin.runCatching {
                        this@drawBackground[x, y] = if (color.alpha >= 0.0001f)
                            bgImageColor else bgColor
                    }
                }
            }
            backgroundBitmap.recycle()
        } else {
            for(i in 0 until width){
                for (j in 0 until height){
                    if (!isActive()){
                        throw CancellationException()
                    }
                    kotlin.runCatching {
                        this@drawBackground[i + error / 2, j + error / 2] =
                            options.colors.bitmapBackground.invoke(
                                i, j, options.size, pixelSize
                            )
                    }
                }
            }
        }
    }

    private fun Bitmap.drawQr(
        result: QrRenderResult, options: QrOptions, isActive: () -> Boolean
    ) = with(result) {

        for (x in error/2 until bitMatrix.width - error/2) {
            for (y in error/2 until bitMatrix.height - error/2) {
                if (!isActive()) {
                    throw CancellationException()
                }
                if (!options.shapes.background
                        .invoke(
                            x- error/2,
                            y-error/2,
                            bitMatrix.width-error,
                            pixelSize,
                            Neighbors.Empty
                        )
                ) continue

                val color = options.colors.codeBackground.invoke(
                    x- error/2, y-error/2, bitMatrix.width-error, pixelSize
                )
                kotlin.runCatching {
                    this@drawQr[x + padding, y + padding] = QrUtil.mixColors(
                        color, this@drawQr[x + padding, y + padding], color.alpha / 255f
                    )
                }
            }
        }

        for (x in 0 until bitMatrix.width) {
            for (y in 0 until bitMatrix.height) {
                if (!isActive()) {
                    throw CancellationException()
                }

                val pixel = bitMatrix[x, y].toInt()

                val realX = minOf(x, bitMatrix.width - x - error)
                val realY = minOf(y, bitMatrix.height - y - error)

                val topRightCorner = bitMatrix.width - x < x && bitMatrix.height - y < y

                val color = when {
                    pixel == 1 && !topRightCorner && options.colors.ball !is QrColor.Unspecified &&
                        ball.let {
                           realX  in it.x until it.x + it.size  &&
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
                    else -> null
                }

                if (color != null) {
                    kotlin.runCatching {
                        this@drawQr[x + padding + error / 2, y + padding + error / 2] = color
                    }
                }
            }
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

                    if (!isActive()) {
                        throw CancellationException()
                    }

                    if (bitmapLogo[i, j] != Color.TRANSPARENT) {
                        runCatching {
                            this@drawLogo[logoTopLeft + i, logoTopLeft + j] =
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