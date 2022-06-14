package com.github.alexzhirkevich.customqrgenerator.encoder

import com.github.alexzhirkevich.customqrgenerator.QrOptions
import com.github.alexzhirkevich.customqrgenerator.neighbors
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.style.QrShapeModifier
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

private class ElementData (
    val x : (Int) -> (Int),
    val y : (Int) -> (Int),
    val size : Int,
    val modifier: QrShapeModifier<Boolean>
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
        val input = options.codeShape.apply(initialInput)
        if (input.height != input.width || initialInput.width > input.width) {
            throw IllegalStateException(
                "QrShape transform must return square matrix and cannot reduce matrix size"
            )
        }
        val diff = (input.width - initialInput.width)/2
        val inputSize = input.width
        val padding = (options.size * options.padding.coerceIn(0f, 1f) /2f).roundToInt()
        val outputSize = (options.size - 2 * padding).coerceAtLeast(inputSize)
        val multiple = outputSize / inputSize
        val output = ByteMatrix(outputSize, outputSize)
        output.clear(-1)
        var inputY = 0
        var outputY = 0

        val error =  ((outputSize.toFloat()/inputSize - multiple)* inputSize).roundToInt()

        while (inputY < input.height) {

            var inputX = 0
            var outputX = 0

            while (inputX < input.width) {

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
                            ) 1 else -1
                        }
                    }
                } else {
                    //pixels

                    for (i in outputX until outputX + multiple) {
                        for (j in outputY until outputY + multiple) {
                            output[i, j] = when {
                                !options.codeShape.pixelInShape(inputX,inputY,input) -> -1
                                input[inputX, inputY].toInt() == 1 && options.shapes.darkPixel.invoke(
                                    i - outputX, j - outputY,
                                    multiple, multiple, neighbors
                                ) -> 1
                                options.shapes.lightPixel.invoke(
                                    i - outputX, j - outputY,
                                    multiple, multiple, neighbors
                                ) -> 0
                                else -> -1
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

    private fun ByteMatrix.applyLogo(error : Int ,diff: Int, multiple: Int, padding : Int) {
        if (options.logo != null) {
            val logoSize = ((options.size - 4 * diff * multiple) *
                    options.logo.size * (1 + options.logo.padding))
                .roundToInt()

            val logoTopLeft = (options.size - logoSize - padding * 2) / 2 - error/2

            for (i in 0 until logoSize) {
                for (j in 0 until logoSize) {
                    if (options.logo.shape.invoke(i, j, logoSize, multiple, Neighbors.Empty)) {
                        kotlin.runCatching {
                            this[logoTopLeft + i, logoTopLeft + j] = -1
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