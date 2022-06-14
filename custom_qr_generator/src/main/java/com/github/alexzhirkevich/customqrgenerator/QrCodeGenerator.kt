package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Bitmap

class QrCodeCreationException(cause : Throwable?=null, message: String? = null) :
    Exception(message, cause)

interface QrCodeGenerator {

    fun generateQrCode(text: String, options: QrOptions) : Bitmap

    /**
     * A [generateQrCode] wrap with cancellation support.
     * Should be performed with computation dispatcher.
     * */
    suspend fun generateQrCodeSuspend(text: String, options: QrOptions) : Bitmap
}