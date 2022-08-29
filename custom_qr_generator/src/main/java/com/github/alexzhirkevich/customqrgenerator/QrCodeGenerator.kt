package com.github.alexzhirkevich.customqrgenerator

import android.content.Context
import android.graphics.Bitmap
import com.github.alexzhirkevich.customqrgenerator.style.QrBackground
import com.github.alexzhirkevich.customqrgenerator.style.QrColor
import com.github.alexzhirkevich.customqrgenerator.style.QrColors
import com.github.alexzhirkevich.customqrgenerator.style.QrLogo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

/**
 * This exception can be thrown while generating code
 * */
class QrCodeCreationException(cause : Throwable? = null, message: String? = null) :
    Exception(message, cause)

/**
 * Used to generate QR codes
 * */
interface QrCodeGenerator {

    /**
     * Generate a QR code bitmap.
     *
     * */
    fun generateQrCode(data: QrData, options: QrOptions) : Bitmap

    /**
     * A [generateQrCode] wrap with cancellation support.
     * Always performed with [Dispatchers.Default].
     * */
    suspend fun generateQrCodeSuspend(data: QrData, options: QrOptions) : Bitmap
}

/**
 * Creates an instance of [QrCodeGenerator]
 * */
fun QrCodeGenerator(
    context: Context,
    threadPolicy: ThreadPolicy = ThreadPolicy.SingleThread
) : QrCodeGenerator = QrCodeGeneratorImpl(context,threadPolicy)

/**
 * Thread policy of the [QrCodeGenerator]
 * */
enum class ThreadPolicy {

    /**
     * Use single thread to style qr code
     */
    SingleThread {
        override suspend operator fun invoke(
            width : Int, height : Int, block: (IntRange, IntRange) -> Unit
        ) {
            block((0 until width), (0 until height))
        }
    },

    /**
     * Use 2 threads to style qr code
     */
    DoubleThread {
        override suspend operator fun invoke(
            width : Int, height : Int, block: (IntRange, IntRange) -> Unit
        ) {
            coroutineScope {
                listOf(
                    (0 until width) to (0 until height / 2),
                    (0 until width) to (height / 2 until height),
                ).map {
                    launch(Dispatchers.Default) {
                        block(it.first, it.second)
                    }
                }
            }.joinAll()
        }
    },

    /**
     * Use 4 threads to style qr code
     */
    QuadThread {
        override suspend operator fun invoke(
            width : Int, height : Int, block: (IntRange, IntRange) -> Unit
        ) {
            coroutineScope {
                listOf(
                    (0 until width / 2) to (0 until height / 2),
                    (0 until width / 2) to (height / 2 until height),
                    (width / 2 until width) to (0 until height / 2),
                    (width / 2 until width) to (height / 2 until height)
                ).map {
                    launch(Dispatchers.Default) {
                        block(it.first, it.second)
                    }
                }
            }.joinAll()
        }
    };

    abstract suspend operator fun invoke(
        width : Int, height : Int, block : (IntRange, IntRange) -> Unit
    )
}
