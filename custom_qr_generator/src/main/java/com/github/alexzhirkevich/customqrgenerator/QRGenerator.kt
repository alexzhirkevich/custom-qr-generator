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
        val writer = StyledQRCodeWriter(options)
        try {

            val ecl = when{
                options.logo == null -> options.errorCorrectionLevel
                options.logo.size * (1+options.logo.padding) >= .4 ->
                    ErrorCorrectionLevel.H
                options.logo.size * (1+options.logo.padding) in .2 .. .4
                        && options.errorCorrectionLevel < ErrorCorrectionLevel.Q ->
                    ErrorCorrectionLevel.Q
                options.errorCorrectionLevel < ErrorCorrectionLevel.M ->
                    ErrorCorrectionLevel.M
                else -> options.errorCorrectionLevel
            }


            val bitMatrix = writer.encode(
                text, BarcodeFormat.QR_CODE, options.size, options.size,
                mapOf(
                    EncodeHintType.ERROR_CORRECTION to ecl,
                    EncodeHintType.MARGIN to options.padding
                )
            )

            val bmp = Bitmap.createBitmap(options.size, options.size, Bitmap.Config.ARGB_8888)

//            for (i in 0 until bmp.width) {
//                for (j in 0 until bmp.height) {
                    bmp.setPixels(
                        IntArray(bmp.width * bmp.height) { options.lightColor },
                        0, bmp.width, 0, 0, bmp.width, bmp.height
                    )
//                }
//            }

            if (options.background != null){
                val backgroundBitmap = options.background.drawable
                    .toBitmap(options.size,options.size,Bitmap.Config.ARGB_8888)
                for (x in 0 until options.size){
                    for (y in 0 until options.size){

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

            val offset = options.padding * writer.multiple
            for (x in 0 until options.size) {
                for (y in 0 until options.size) {
                    if (bitMatrix[x,y]){
                        bmp[x,y] = options.darkColor
                    } else {
                        if (options.lightColor != Color.TRANSPARENT && (x in offset until bmp.width - offset &&
                            y in offset until bmp.height - offset)

                            && options.style.shape.isDark(x-offset,y-offset,
                                ((options.size-offset * 2f)/writer.multiple).roundToInt(),
                                writer.multiple, Neighbors.Empty
                            )) {
                            bmp[x, y] = Color.rgb(
                                (options.lightColor.red * 0.75 + bmp[x, y].red * 0.25).roundToInt(),
                                (options.lightColor.green * 0.75 + bmp[x, y].green * 0.25).roundToInt(),
                                (options.lightColor.blue * 0.75 + bmp[x, y].blue * 0.25).roundToInt()
                            )
                        }
                    }
//                    bmp.setPixel(x, y, if (bitMatrix[x, y])
//                        options.darkColor else options.lightColor)
                }
            }
            if (options.logo != null){
                val logoSize = (options.size * options.logo.size ).roundToInt()
                val bitmapLogo = options.logo.drawable
                    .toBitmap(logoSize,logoSize, Bitmap.Config.ARGB_8888)
                val logoTopLeft = (options.size - logoSize)/2

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

