package com.github.alexzhirkevich.customqrgenerator

import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import kotlin.Throws
import com.google.zxing.WriterException
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.EncodeHintType
import com.google.zxing.Writer
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.QRCode
import com.google.zxing.qrcode.encoder.Encoder
import java.lang.IllegalStateException
import kotlin.math.roundToInt

internal class StyledQRCodeWriter(private val options: QrOptions) : Writer {

    companion object {
        private const val QUIET_ZONE_SIZE = 4
    }

    @Throws(WriterException::class)
    override fun encode(
        contents: String,
        format: BarcodeFormat,
        width: Int,
        height: Int
    ): BitMatrix {
        return encode(contents, format, width, height, null)
    }

    var multiple : Int = 0
    private set

    @Throws(WriterException::class)
    override fun encode(
        contents: String,
        format: BarcodeFormat,
        width: Int,
        height: Int,
        hints: Map<EncodeHintType?, *>?
    ): BitMatrix {
        require(contents.isNotEmpty()) { "Found empty contents" }
        require(format == BarcodeFormat.QR_CODE) { "Can only encode QR_CODE, but got $format" }
        require(!(width < 0 || height < 0)) {
            "Requested dimensions are too small: " + width + 'x' +
                    height
        }
        var errorCorrectionLevel = ErrorCorrectionLevel.L
        var quietZone = QUIET_ZONE_SIZE
        if (hints != null) {
            if (hints.containsKey(EncodeHintType.ERROR_CORRECTION)) {
                errorCorrectionLevel =
                    ErrorCorrectionLevel.valueOf(hints[EncodeHintType.ERROR_CORRECTION].toString())
            }
            if (hints.containsKey(EncodeHintType.MARGIN)) {
                quietZone = hints[EncodeHintType.MARGIN].toString().toInt()
            }
        }
        val code = Encoder.encode(contents, errorCorrectionLevel, hints)
        return renderResult(code, width, height, quietZone, options)
    }


        // Note that the input matrix uses 0 == white, 1 == black, while the output matrix uses
        // 0 == black, 255 == white (i.e. an 8 bit greyscale bitmap).
        private fun renderResult(code: QRCode, width: Int, height: Int, quietZone: Int, options: QrOptions): BitMatrix {
            val input = code.matrix ?: throw IllegalStateException()
            val qrWidth = input.width + quietZone * 2
            val qrHeight = input.height + quietZone * 2
            val outputWidth = width.coerceAtLeast(qrWidth)
            val outputHeight = height.coerceAtLeast(qrHeight)
            multiple = (outputWidth / qrWidth).coerceAtMost(outputHeight / qrHeight)
            // Padding includes both the quiet zone and the extra white pixels to accommodate the requested
            // dimensions. For example, if input is 25x25 the QR will be 33x33 including the quiet zone.
            // If the requested size is 200x160, the multiple will be 4, for a QR of 132x132. These will
            // handle all the padding from 100x100 (the actual QR) up to 200x160.
            val leftPadding = (outputWidth - input.width * multiple) / 2
            val topPadding = (outputHeight - input.height * multiple) / 2
            val output = BitMatrix(outputWidth, outputHeight)
            var inputY = 0
            var outputY = topPadding
            while (inputY < input.height) {

                var inputX = 0
                var outputX = leftPadding
                while (inputX < input.width) {

                    val setFrameOrBall = { i : Int, j : Int ->
                        output.set(
                            topPadding + inputX*multiple + i,
                            leftPadding + inputY*multiple + j
                        )
                    }

                    val unsetFrameOrBall = { i : Int, j : Int ->
                        output.unset(
                            topPadding + inputX*multiple + i,
                            leftPadding + inputY*multiple + j
                        )
                    }
                    val neighbors = input.neighbors(inputX,inputY)

                    kotlin.run {

                        if (inputX in 0..6 && inputY in 0..6) {
                            // top left eye

                            if (inputX in 2..4 && inputY in 2..4) {
                                //top left ball
                                for (i in 0 until multiple) {
                                    for (j in 0 until multiple) {
                                        if (options.style.ball.isDark(
                                            (inputX - 2) * multiple + i,
                                            (inputY - 2) * multiple + j,
                                            3,
                                            multiple,
                                            neighbors
                                        )){
                                            setFrameOrBall(i,j)
                                        } else {
                                            unsetFrameOrBall(i,j)
                                        }
                                    }
                                }
                                return@run
                            }
                            // top left frame
                            for (i in 0 until multiple) {
                                for (j in 0 until multiple) {
                                    if (options.style.frame.isDark(
                                            inputX * multiple + i,
                                            inputY * multiple + j,
                                            7,
                                            multiple,
                                            neighbors
                                        )
                                    ) {
                                        setFrameOrBall(i,j)
                                    } else {
                                        unsetFrameOrBall(i,j)
                                    }
                                }
                            }

                            return@run
                        }
                        if (input.width - inputX-1 in 0..6 && inputY in 0..6){
                            // top right eye

                            if (input.width - inputX-1 in 2..4 && inputY in 2..4){
                                // top right ball
                                for (i in 0 until multiple){
                                    for (j in 0 until multiple){
                                        if (options.style.ball.isDark(
                                                (input.width - inputX-2) * multiple - i,
                                                (inputY-2) * multiple + j,
                                                3,
                                                multiple,
                                                neighbors
                                            )
                                        ) {
                                            setFrameOrBall(i,j)
                                        } else {
                                            unsetFrameOrBall(i,j)
                                        }
                                    }
                                }
                                return@run
                            }
                            // top right frame
                            for (i in 0 until multiple){
                                for (j in 0 until multiple){
                                    if (options.style.frame.isDark(
                                            (input.width - inputX) * multiple - i,
                                            inputY * multiple + j,
                                            7,
                                            multiple,
                                            neighbors
                                        )
                                    ) {
                                        setFrameOrBall(i,j)
                                    } else {
                                        unsetFrameOrBall(i,j)
                                    }
                                }
                            }

                            return@run
                        }

                        if (inputX in 0..6 && input.height -inputY-1 in 0..6){
                            // bottom left eye

                            if (inputX in 2..4 && input.height- inputY-1 in 2..4){
                                // bottom left ball

                                for (i in 0 until multiple){
                                    for (j in 0 until multiple){
                                        if (options.style.ball.isDark(
                                                (inputX-2) * multiple + i,
                                                (input.height - inputY-2) * multiple - j,
                                                3,
                                                multiple,
                                                neighbors
                                            )
                                        ) {
                                            setFrameOrBall(i,j)
                                        } else {
                                            unsetFrameOrBall(i,j)
                                        }
                                    }
                                }
                                return@run
                            }
                            // bottom left frame
                            for (i in 0 until multiple){
                                for (j in 0 until multiple){
                                    if (options.style.frame.isDark(
                                            inputX * multiple + i,
                                            (input.height- inputY) * multiple - j,
                                            7,
                                            multiple,
                                            neighbors
                                        )
                                    ) {
                                        setFrameOrBall(i,j)
                                    } else {
                                        unsetFrameOrBall(i,j)
                                    }
                                }
                            }
                            return@run
                        }

                        //pixels
                        if (input[inputX, inputY].toInt() == 1) {
                            for (i in outputX until outputX + multiple) {
                                for (j in outputY until outputY + multiple) {
                                    if (options.style.pixel.isDark(
                                            i - outputX,
                                            j - outputY,
                                            1,
                                            multiple,
                                            neighbors)){
                                        output.set(i,j)
                                    }
                                }
                            }
                        }
                    }
                    inputX++
                    outputX += multiple
                }
                inputY++
                outputY += multiple
            }
            //logo
            if (options.logo != null){
                val logoSize = (options.size * options.logo.size * (1 + options.logo.padding))
                    .roundToInt()

                val logoTopLeft = (options.size - logoSize)/2

                for (i in 0 until logoSize){
                    for (j in 0 until logoSize){
                        if (options.logo.shape.isDark(
                                i,j,(logoSize.toDouble() / multiple).roundToInt(),
                                multiple, Neighbors.Empty
                            )){
                            output.unset(logoTopLeft + i, logoTopLeft + j)
                        }
                    }
                }
            }
            return output
        }
}

internal fun ByteMatrix.neighbors(i : Int, j : Int) : Neighbors {
    val topLeft = kotlin.runCatching {
        this[i - 1, j - 1].toInt() == 1
    }.getOrDefault(false)
    val topRight = kotlin.runCatching {
        this[i - 1, j + 1].toInt() == 1
    }.getOrDefault(false)
    val top = kotlin.runCatching {
        this[i - 1, j].toInt() == 1
    }.getOrDefault(false)
    val left = kotlin.runCatching {
        this[i, j - 1].toInt() == 1
    }.getOrDefault(false)
    val right = kotlin.runCatching {
        this[i, j + 1].toInt() == 1
    }.getOrDefault(false)
    val bottomLeft = kotlin.runCatching {
        this[i+1, j - 1].toInt() == 1
    }.getOrDefault(false)
    val bottomRight = kotlin.runCatching {
        this[i+1, j + 1].toInt() == 1
    }.getOrDefault(false)
    val bottom = kotlin.runCatching {
        this[i+1, j].toInt() == 1
    }.getOrDefault(false)
    return Neighbors(
        topLeft, topRight, left, top, right, bottomLeft, bottom, bottomRight
    )
}