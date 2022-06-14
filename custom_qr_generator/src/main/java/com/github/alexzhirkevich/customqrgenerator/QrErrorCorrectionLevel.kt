package com.github.alexzhirkevich.customqrgenerator

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel


/**
 * QR code technology allows you to read encoded information even if
 * part of the QR code image is damaged. It's also allow to have logo
 * inside the code as a part of "damage".
 * */
enum class QrErrorCorrectionLevel(
    internal val lvl : ErrorCorrectionLevel
) {

    /**
     * Minimum possible level will be used.
     * */
    Auto(ErrorCorrectionLevel.L),

    /**
     * ~7% of QR code can be damaged (or used as logo).
     * QR code might be unreadable if logo is too big.
     * If your code has logo, it's better to use [Auto].
     * */
    Low(ErrorCorrectionLevel.L),

    /**
     * ~15% of QR code can be damaged (or used as logo).
     * QR code might be unreadable if logo is too big.
     * If your code has logo, it's better to use [Auto].
     * */
    Medium(ErrorCorrectionLevel.M),

    /**
     * ~25% of QR code can be damaged (or used as logo).
     * QR code might be unreadable if logo is too big.
     * If your code has logo, it's better to use [Auto].
     * */
    MediumHigh(ErrorCorrectionLevel.Q),

    /**
     * ~30% of QR code can be damaged (or used as logo).
     * QR code might be unreadable if logo is too big.
     * If your code has logo, it's better to use [Auto].
     * */
    High(ErrorCorrectionLevel.H)

}