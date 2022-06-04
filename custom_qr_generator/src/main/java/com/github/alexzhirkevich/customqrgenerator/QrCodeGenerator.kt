package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Bitmap

class QrCodeCreationException(cause : Throwable?=null, message: String? = null) :
    Exception(message, cause)

interface QrCodeCreator {

    fun createQrCode(text: String, options: QrOptions) : Bitmap
}