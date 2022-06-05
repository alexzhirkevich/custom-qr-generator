package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.*
import androidx.core.graphics.drawable.toBitmap
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlin.math.roundToInt


class QRGenerator : QrCodeCreator {

    override fun createQrCode(text: String, options: QrOptions): Bitmap {

        try {

            val ecl = when{
                options.logo == null -> options.errorCorrectionLevel
                options.logo.size * (1+options.logo.padding) >= .4 ->
                    ErrorCorrectionLevel.H
                options.logo.size * (1+options.logo.padding) in .25 .. .4
                        && options.errorCorrectionLevel < ErrorCorrectionLevel.Q ->
                    ErrorCorrectionLevel.Q
                options.errorCorrectionLevel < ErrorCorrectionLevel.M ->
                    ErrorCorrectionLevel.M
                else -> options.errorCorrectionLevel
            }

            val writer = QrEncoder(options.copy(errorCorrectionLevel = ecl))

            val padding = options.padding.coerceIn(0, options.size/2)

            val (bitMatrix, pixelSize, shapeIncrease) =  writer.encode(text)

            val bmp = Bitmap.createBitmap(options.size, options.size, Bitmap.Config.ARGB_8888)

            bmp.setPixels(
                IntArray(bmp.width * bmp.height) { options.lightColor },
                0, bmp.width, 0, 0, bmp.width, bmp.height
            )

            if (options.background != null){
                val backgroundBitmap = options.background.drawable
                    .toBitmap(bmp.width, bmp.height,Bitmap.Config.ARGB_8888)
                for (x in 0 until bmp.width){
                    for (y in 0 until bmp.height){

                        val color =  backgroundBitmap[x,y].toColorLong()
                        if (color != Color.TRANSPARENT.toLong()) {
                            bmp[x, y] = Color.argb(
                                (options.background.alpha * 255).roundToInt(),
                                (color.red * 255).roundToInt(),
                                (color.green * 255).roundToInt(),
                                (color.blue * 255).roundToInt()
                            )
                        }
                    }
                }
                backgroundBitmap.recycle()
            }

            for (x in padding until bmp.width - padding) {
                for (y in padding until bmp.height - padding) {
                    if (bitMatrix[x-padding, y-padding]){
                        bmp[x,y] = options.darkColor
                    } else {
                        if (options.lightColor != Color.TRANSPARENT &&
                            options.style.bgShape.isDark(
                                x-padding, y-padding,options.size - 2 * padding,
                                pixelSize,
                                Neighbors.Empty,

                            )) {
                            bmp[x, y] = Color.rgb(
                                (options.lightColor.red * 0.75 + bmp[x, y].red * 0.25).roundToInt(),
                                (options.lightColor.green * 0.75 + bmp[x, y].green * 0.25).roundToInt(),
                                (options.lightColor.blue * 0.75 + bmp[x, y].blue * 0.25).roundToInt()
                            )
                        }
                    }
                }
            }
            if (options.logo != null){
                val logoSize = ((bmp.width - shapeIncrease*4) * options.logo.size).roundToInt()
                val bitmapLogo = options.logo.drawable
                    .toBitmap(logoSize,logoSize, Bitmap.Config.ARGB_8888)
                val logoTopLeft = (bmp.width - logoSize)/2

                for (i in 0 until bitmapLogo.width){
                    for (j in 0 until bitmapLogo.height){
                        if (bitmapLogo[i,j] != Color.TRANSPARENT)
                            bmp[logoTopLeft+i, logoTopLeft+j] = bitmapLogo[i, j]
                    }
                }
                bitmapLogo.recycle()
            }

            return bmp
        } catch (e: Exception) {
            throw QrCodeCreationException(cause = e)
        }
    }
}

