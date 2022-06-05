package com.github.alexzhirkevich.customqrgenerator

import com.github.alexzhirkevich.customqrgenerator.style.AsPixels
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.style.QrFrameStyle
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random


data class QrRenderResult(
    val bitMatrix: BitMatrix,
    val pixelSize : Int,
    val shapeIncrease : Int
)

internal class QrEncoder(private val options: QrOptions)  {

    companion object {
        private const val FRAME_SIZE = 7
        private const val BALL_SIZE = 3
    }

    fun encode(
        contents: String,
    ): QrRenderResult {
        require(contents.isNotEmpty()) { "Found empty contents" }
        val code = Encoder.encode(contents, options.errorCorrectionLevel, null)
        return renderResult(code)
    }


        // Note that the input matrix uses 0 == white, 1 == black, while the output matrix uses
        // 0 == black, 255 == white (i.e. an 8 bit greyscale bitmap).
        private fun renderResult(code: QRCode): QrRenderResult {
            val initialInput = (code.matrix ?: throw IllegalStateException())
            val input = options.style.qrShape.apply(initialInput)
            val diff = abs(input.width - initialInput.width)/2

            val qrWidth = input.width
            val qrHeight = input.height
            val outputWidth = (options.size - options.padding * 2).coerceAtLeast(qrWidth)
            val outputHeight = (options.size - options.padding * 2).coerceAtLeast(qrHeight)
            val multiple = (outputWidth / qrWidth).coerceAtMost(outputHeight / qrHeight)
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

                    val neighbors = input.neighbors(inputX,inputY)

                    fun setFrameOrBall(i : Int, j : Int) {
                        output.set(
                            topPadding + inputX*multiple + i,
                            leftPadding + inputY*multiple + j
                        )
                    }

                    fun unsetFrameOrBall(i : Int, j : Int) {
                        output.unset(
                            topPadding + inputX*multiple + i,
                            leftPadding + inputY*multiple + j
                        )
                    }

                    fun isBallAsPixel(i : Int, j : Int) : Boolean =
                        if (options.style.ball is AsPixels) {
                            val pixelStyle = options.style.ball.pixelStyle
                            if (pixelStyle.isDark(i, j,multiple, multiple,neighbors )) {
                                setFrameOrBall(i, j)
                            } else {
                                unsetFrameOrBall(i, j)
                            }
                            true
                        } else false

                    fun isFrameAsPixel(i : Int, j : Int, frameI : Int, frameJ : Int) : Boolean =
                        if (options.style.frame is AsPixels) {
                            val pixelStyle = options.style.frame.pixelStyle
                            if (QrFrameStyle.Default
                                    .isDark(frameI, frameJ, FRAME_SIZE*multiple,multiple, neighbors) &&
                                pixelStyle
                                    .isDark(i, j, multiple,multiple, neighbors)
                            ) {
                                setFrameOrBall(i, j)
                            } else {
                                unsetFrameOrBall(i, j)
                            }
                            true
                        } else false

                    kotlin.run {

                        if (inputX- diff in 0 until FRAME_SIZE &&
                            inputY -diff in 0 until FRAME_SIZE) {
                            // top left eye

                            if (inputX - diff in (FRAME_SIZE - BALL_SIZE)/2  until (FRAME_SIZE - BALL_SIZE)/2 + BALL_SIZE
                                && inputY - diff in  (FRAME_SIZE - BALL_SIZE)/2  until (FRAME_SIZE - BALL_SIZE)/2 + BALL_SIZE) {
                                //top left ball
                                for (i in 0 until multiple) {
                                    for (j in 0 until multiple) {

                                        if (isBallAsPixel(i,j)){
                                            continue
                                        }

                                        if (options.style.ball.isDark(
                                                 (inputX -diff- (FRAME_SIZE - BALL_SIZE)/2 ) * multiple + i,
                                                (inputY -diff- (FRAME_SIZE - BALL_SIZE)/2 ) * multiple + j,
                                                BALL_SIZE*multiple,
                                                multiple,
                                                neighbors,
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

                                    val frameI = (inputX - diff) * multiple + i
                                    val frameJ = (inputY - diff) * multiple + j

                                    if (isFrameAsPixel(i,j,frameI, frameJ)){
                                        continue
                                    }

                                    if (options.style.frame.isDark(frameI, frameJ,
                                            FRAME_SIZE*multiple, multiple, neighbors)){
                                        setFrameOrBall(i,j)
                                    } else {
                                        unsetFrameOrBall(i,j)
                                    }
                                }
                            }

                            return@run
                        }
                        if (input.width - inputX - 1 - diff in 0 until FRAME_SIZE &&
                            inputY - diff in 0 until FRAME_SIZE){
                            // top right eye

                            if (input.width - inputX-1 - diff in  (FRAME_SIZE - BALL_SIZE)/2 until (FRAME_SIZE - BALL_SIZE)/2 + BALL_SIZE
                                && inputY - diff in (FRAME_SIZE - BALL_SIZE)/2 until (FRAME_SIZE - BALL_SIZE)/2 + BALL_SIZE){
                                // top right ball
                                for (i in 0 until multiple){
                                    for (j in 0 until multiple){

                                        if (isBallAsPixel(i,j)){
                                            continue
                                        }

                                        if (options.style.ball.isDark(
                                                (input.width - inputX - diff - (FRAME_SIZE - BALL_SIZE)/2 ) * multiple - i,
                                                (inputY-(FRAME_SIZE - BALL_SIZE)/2 - diff) * multiple + j,
                                                BALL_SIZE*multiple,
                                                multiple,
                                                neighbors,
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
                            for (i  in 0 until multiple){
                                for (j in 0 until multiple){

                                    val frameI = (input.width - inputX - diff) * multiple - i
                                    val frameJ = (inputY - diff) * multiple + j

                                    if (isFrameAsPixel(i,j,frameI, frameJ)){
                                        continue
                                    }

                                    if (options.style.frame.isDark(frameI, frameJ,
                                            FRAME_SIZE*multiple, multiple, neighbors)
                                    ) {
                                        setFrameOrBall(i,j)
                                    } else {
                                        unsetFrameOrBall(i,j)
                                    }
                                }
                            }

                            return@run
                        }

                        if (inputX - diff in 0..6 && input.height -inputY-1-diff in 0..6){
                            // bottom left eye

                            if (inputX - diff in 2..4 && input.height- inputY-1 -diff in 2..4){
                                // bottom left ball

                                for (i in 0 until multiple){
                                    for (j in 0 until multiple){

                                        if (isBallAsPixel(i,j)){
                                            continue
                                        }

                                        if (options.style.ball.isDark(
                                                (inputX-2 - diff) * multiple + i,
                                                (input.height - inputY-2 - diff) * multiple - j,
                                                BALL_SIZE*multiple,
                                                multiple,
                                                neighbors,
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

                                    val frameI = (inputX - diff) * multiple + i
                                    val frameJ = (input.height - inputY - diff) * multiple - j

                                    if (isFrameAsPixel(i,j, frameI, frameJ)){
                                        continue
                                    }

                                    if (options.style.frame.isDark(frameI,frameJ,
                                            FRAME_SIZE*multiple, multiple, neighbors)
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
                                            multiple,
                                            multiple,
                                            neighbors
                                        )){
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
                val logoSize = ((options.size- 4*diff*multiple) * options.logo.size * (1 + options.logo.padding))
                    .roundToInt()

                val padding = options.padding.coerceIn(0,options.size/2)
                val logoTopLeft = (options.size - logoSize - padding * 2)/2

                for (i in 0 until logoSize){
                    for (j in 0 until logoSize){
                        if (options.logo.shape.isDark(
                                i, j,
                                logoSize,
                                multiple,
                                Neighbors.Empty,
                            )){
                            output.unset(logoTopLeft + i, logoTopLeft + j)
                        }
                    }
                }
            }
            return QrRenderResult(output,multiple,diff*multiple)
        }
}

internal fun ByteMatrix.extendToRound(random: Random?) : ByteMatrix {
    if (width != height)
        throw IllegalStateException("Non-square ByteMatrix can not be extended to round")

    val added = (width *1.05 * sqrt(2.0)).roundToInt()/4

    val newSize = width + 2*added
    val newMatrix = ByteMatrix(newSize,newSize)
    if (random != null) {

        val center = newSize / 2f

        for (i in 0 until newSize) {
            for (j in 0 until newSize) {
                if (random.nextBoolean() &&
                    (i < added-1 ||
                            j < added-1 ||
                            i > added + width ||
                            j > added + width ) &&
                            sqrt((center-i) *(center-i)+(center-j)*(center-j))<center
                ){
                    newMatrix.set(i, j, 1)

                }
            }
        }
    }

    for(i in 0 until width){
        for(j in 0 until height){
            newMatrix[added+i,added+j] = this[i,j]
        }
    }
    return newMatrix
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