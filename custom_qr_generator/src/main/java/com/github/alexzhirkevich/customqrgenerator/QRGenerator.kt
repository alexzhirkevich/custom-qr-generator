package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.alpha
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.get
import androidx.core.graphics.set
import com.github.alexzhirkevich.customqrgenerator.encoder.QrEncoder
import com.github.alexzhirkevich.customqrgenerator.encoder.QrRenderResult
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.style.QrColor
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.*
import kotlin.math.roundToInt

class QrGenerator(
    private val threadPolicy : ThreadPolicy = ThreadPolicy
        .SingleThread
) : QrCodeGenerator {

    enum class ThreadPolicy {

        SingleThread {
            override fun invoke(size: Int): List<Pair<IntRange, IntRange>> =
                listOf((0 until size) to (0 until size))
        },

        DoubleThread{
            override fun invoke(size: Int): List<Pair<IntRange, IntRange>> =
                listOf(
                    (0 until size) to (0 until size/2),
                    (0 until size) to (size/2 until size),
                )
            },

        QuadThread{
            override fun invoke(size: Int): List<Pair<IntRange, IntRange>> =
                listOf(
                    (0 until size/2) to (0 until size/2),
                    (0 until size/2) to (size/2 until size),
                    (size/2 until size) to (0 until size/2),
                    (size/2 until size) to (size/2 until size)
                )
        };

        abstract operator fun invoke(size : Int) : List<Pair<IntRange, IntRange>>
    }

    override fun generateQrCode(data: QrData, options: QrOptions): Bitmap =
        kotlin.runCatching {
            val encoder = QrEncoder(options.copy(errorCorrectionLevel = options.actualEcl))
            val result = encoder.encode(data.encode())
            runBlocking {
                createQrCodeInternal(result, options)
            }
        }.getOrElse {
            throw QrCodeCreationException(it)
        }

    override suspend fun generateQrCodeSuspend(data: QrData, options: QrOptions): Bitmap =
        withContext(Dispatchers.Default) {
            kotlin.runCatching {
                val encoder = QrEncoder(options.copy(errorCorrectionLevel = options.actualEcl))
                val result = encoder.encodeSuspend(data.encode())
                createQrCodeInternal(result, options)
            }.getOrElse {
                throw if (it is CancellationException)
                     it else QrCodeCreationException(cause = it)
            }
        }

    private suspend fun createQrCodeInternal(
       result: QrRenderResult, options: QrOptions
    ) : Bitmap {

        val bmp = Bitmap.createBitmap(
            options.size, options.size,
            Bitmap.Config.ARGB_8888
        )

        return bmp.apply {
            drawCode(result, options)
            drawLogo(result, options)
        }
    }

    private suspend fun Bitmap.drawCode(result: QrRenderResult, options: QrOptions) = coroutineScope{
        with(result) {
            val bgBitmap = options.background?.drawable
                ?.toBitmap(width, height, Bitmap.Config.ARGB_8888)

            val ranges = threadPolicy(width)

            ranges.map {
                launch(Dispatchers.Default) {

                    for (x in it.first) {
                        for (y in it.second) {
                            ensureActive()

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
                                            x,y, bitMatrix.width, pixelSize)
                            }
                        }
                    }
                }
            }.joinAll()
            bgBitmap?.recycle()
        }
    }

    private suspend fun Bitmap.drawLogo(
        renderResult: QrRenderResult, options: QrOptions
    ) = with(renderResult){
        coroutineScope {
            if (options.logo != null) {
                val logoSize = ((width - shapeIncrease * 4) * options.logo.size).roundToInt()
                val bitmapLogo = options.logo.drawable
                    .toBitmap(logoSize, logoSize, Bitmap.Config.ARGB_8888)
                val logoTopLeft = (width - logoSize) / 2

                for (i in 0 until bitmapLogo.width) {
                    for (j in 0 until bitmapLogo.height) {

                        ensureActive()

                        if (!options.logo.shape.invoke(i, j, bitmapLogo.width,
                                pixelSize, Neighbors.Empty)
                        ) {
                            continue
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