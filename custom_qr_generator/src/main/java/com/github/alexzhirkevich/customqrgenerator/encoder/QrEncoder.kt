package com.github.alexzhirkevich.customqrgenerator.encoder

import com.github.alexzhirkevich.customqrgenerator.QrOptions
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.style.QrShapeModifier
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

private class ElementData (
    val x : (Int) -> (Int),
    val y : (Int) -> (Int),
    val size : Int,
    val modifier: QrShapeModifier
)

internal class QrEncoder(private val options: QrOptions)  {

    companion object {
        const val FRAME_SIZE = 7
        const val BALL_SIZE = 3
    }

    fun encode(
        contents: String,
    ): QrRenderResult {
        require(contents.isNotEmpty()) { "Found empty contents" }
        val code = Encoder.encode(contents, options.errorCorrectionLevel.lvl, null)
        return renderResult(code) {true}
    }

    suspend fun encodeSuspend(
        contents: String,
    ): QrRenderResult = coroutineScope {

        require(contents.isNotEmpty()) { "Found empty contents" }
        val code = Encoder.encode(contents, options.errorCorrectionLevel.lvl, null)
        renderResult(code, coroutineContext::isActive)
    }

    // Note that the input matrix uses 0 == white, 1 == black, while the output matrix uses
    // 0 == black, 255 == white (i.e. an 8 bit greyscale bitmap).
    private fun renderResult(code: QRCode, isActive : () -> Boolean): QrRenderResult {
        val initialInput = (code.matrix ?: throw IllegalStateException())
            .toQrMatrix()
        val input = options.codeShape.apply(initialInput)
        val diff = (input.size - initialInput.size)/2
        val inputSize = input.size
        val padding = (options.size * options.padding.coerceIn(0f, 1f) /2f).roundToInt()
        val outputSize = (options.size - 2 * padding).coerceAtLeast(inputSize)
        val multiple = outputSize / inputSize
        val output = QrCodeMatrix(outputSize)
        var inputY = 0
        var outputY = 0

        val error =  ((outputSize.toFloat()/inputSize - multiple)* inputSize).roundToInt()

        while (inputY < input.size) {

            var inputX = 0
            var outputX = 0

            while (inputX < input.size) {

                if (!isActive()){
                    throw CancellationException()
                }

                val elementData = elementDataOrNull(
                    inputX, inputY, diff, multiple, inputSize
                )

                val neighbors = input.neighbors(inputX,inputY)

                if (elementData != null) {
                    for (i in 0 until multiple) {
                        for (j in 0 until multiple) {
                            output[inputX * multiple + i, inputY * multiple + j] =
                                if (elementData.modifier.invoke(
                                    elementData.x(i),
                                    elementData.y(j),
                                    elementData.size,
                                    multiple,
                                    neighbors
                                )
                            ) QrCodeMatrix.PixelType.DarkPixel else QrCodeMatrix.PixelType.Background
                        }
                    }
                } else {
                    //pixels

                    for (i in outputX until outputX + multiple) {
                        for (j in outputY until outputY + multiple) {
                            output[i, j] = when {
                                !options.codeShape.pixelInShape(inputX,inputY,input) -> QrCodeMatrix.PixelType.Background
                                input[inputX, inputY] == QrCodeMatrix.PixelType.DarkPixel && options.shapes.darkPixel.invoke(
                                    i - outputX, j - outputY,
                                    multiple, multiple, neighbors
                                ) -> QrCodeMatrix.PixelType.DarkPixel
                                options.shapes.lightPixel.invoke(
                                    i - outputX, j - outputY,
                                    multiple, multiple, neighbors
                                ) -> QrCodeMatrix.PixelType.LightPixel
                                else -> QrCodeMatrix.PixelType.Background
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

        if (options.logo != null){
            output.applyLogo(error,diff, multiple, padding)
        }

        val frame = Rectangle(
            diff * multiple,
            diff * multiple,
            FRAME_SIZE * multiple
        )

        val ball = Rectangle(
            frame.x + (FRAME_SIZE - BALL_SIZE)/2 * multiple,
            frame.y + (FRAME_SIZE - BALL_SIZE)/2* multiple,
            BALL_SIZE * multiple
        )

        return QrRenderResult(output, padding, multiple,diff*multiple, frame, ball, error)
    }

    private fun QrCodeMatrix.applyLogo(error : Int, diff: Int, multiple: Int, padding : Int) {
        if (options.logo != null) {
            val logoSize = ((options.size - 4 * diff * multiple) *
                    options.logo.size * (1 + options.logo.padding))
                .roundToInt()

            val logoTopLeft = (options.size - logoSize - padding * 2) / 2 - error/2

            for (i in 0 until logoSize) {
                for (j in 0 until logoSize) {
                    if (options.logo.shape.invoke(i, j, logoSize, multiple, Neighbors.Empty)) {
                        kotlin.runCatching {
                            this[logoTopLeft + i, logoTopLeft + j] = QrCodeMatrix.PixelType.Background
                        }
                    }
                }
            }
        }
    }

    private fun elementDataOrNull(
        inputX : Int, inputY : Int, diff :Int, multiple : Int, inputSize : Int
    ) = when {

        //top left ball
        inputX - diff in 2 until 5 && inputY - diff in 2  until 5 ->
        ElementData(
            {(inputX -diff- 2) * multiple + it},
            {(inputY -diff- 2) * multiple + it},
            3 * multiple,
            options.shapes.ball)

        // top left frame
        inputX- diff in 0 until 7 && inputY -diff in 0 until 7 ->
        ElementData(
            {(inputX - diff) * multiple + it},
            {(inputY - diff) * multiple + it },
            7 * multiple,

            options.shapes.frame
        )

        //top right ball
        inputSize - inputX-1 - diff in 2 until 5 && inputY - diff in 2 until 5->
        ElementData(
            {(inputSize - inputX - diff - 2) * multiple - it},
            {(inputY- 2 - diff) * multiple + it},
            3 * multiple,
            options.shapes.ball
        )

        //top right frame
        inputSize - inputX - 1 - diff in 0 until 7 && inputY - diff in 0 until 7 ->
        ElementData(
            {(inputSize - inputX - diff) * multiple - it},
            {(inputY - diff) * multiple + it},
            7 * multiple,
            options.shapes.frame
        )

        //bottom ball
        inputX - diff in 2 until 5 && inputSize- inputY-1 -diff in 2 until 5 ->
        ElementData(
            {(inputX-2 - diff) * multiple + it},
            {(inputSize - inputY-2 - diff) * multiple - it},
            3 * multiple,
            options.shapes.ball
        )
        //bottom frame
        inputX - diff in 0 until 7 && inputSize -inputY-1-diff in 0 until 7 ->
        ElementData(
            {(inputX - diff) * multiple + it},
            { (inputSize - inputY - diff) * multiple - it},
            7 * multiple,
            options.shapes.frame
        )
        else -> null
    }
}

fun ByteMatrix.toQrMatrix() : QrCodeMatrix {
    if (width != height)
        throw IllegalStateException("Non-square qr byte matrix")

    return QrCodeMatrix(width).apply {
        for (i in 0 until width){
            for (j in 0 until width){
                this[i,j] = if (this@toQrMatrix[i,j].toInt() == 1)
                    QrCodeMatrix.PixelType.DarkPixel
                else QrCodeMatrix.PixelType.Background
            }
        }
    }
}

internal fun QrCodeMatrix.neighbors(i : Int, j : Int) : Neighbors {

    val topLeft = kotlin.runCatching {
        this[i - 1, j - 1] == QrCodeMatrix.PixelType.DarkPixel
    }.getOrDefault(false)
    val topRight = kotlin.runCatching {
        this[i - 1, j + 1] == QrCodeMatrix.PixelType.DarkPixel
    }.getOrDefault(false)
    val top = kotlin.runCatching {
        this[i - 1, j] == QrCodeMatrix.PixelType.DarkPixel
    }.getOrDefault(false)
    val left = kotlin.runCatching {
        this[i, j - 1] == QrCodeMatrix.PixelType.DarkPixel
    }.getOrDefault(false)
    val right = kotlin.runCatching {
        this[i, j + 1] == QrCodeMatrix.PixelType.DarkPixel
    }.getOrDefault(false)
    val bottomLeft = kotlin.runCatching {
        this[i+1, j - 1] == QrCodeMatrix.PixelType.DarkPixel
    }.getOrDefault(false)
    val bottomRight = kotlin.runCatching {
        this[i+1, j + 1] == QrCodeMatrix.PixelType.DarkPixel
    }.getOrDefault(false)
    val bottom = kotlin.runCatching {
        this[i+1, j] == QrCodeMatrix.PixelType.DarkPixel
    }.getOrDefault(false)
    return Neighbors(
        topLeft, topRight, left, top, right, bottomLeft, bottom, bottomRight
    )
}