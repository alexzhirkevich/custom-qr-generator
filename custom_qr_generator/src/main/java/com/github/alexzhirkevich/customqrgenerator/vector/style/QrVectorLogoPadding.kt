package com.github.alexzhirkevich.customqrgenerator.vector.style

/**
 * Type of padding applied to the logo.
 * Padding applied even if logo drawable is not specified.
 *
 * Prefer empty padding if your qr code encodes large amount of data
 * to avoid performance issues.
 * */
sealed interface QrVectorLogoPadding {

    val value : Float


    /**
     * Logo will be drawn on top of QR code without any padding.
     * QR code pixels might be visible through transparent logo.
     *
     * Prefer empty padding if your qr code encodes large amount of data
     * to avoid performance issues.
     * */
    object Empty : QrVectorLogoPadding {
        override val value: Float get() = 0f
    }


    /**
     * Padding will be applied directly according to the shape of logo.
     * Some QR code pixels can be cut.
     *
     * WARNING: this padding can cause performance issues for qr codes with
     * large amount out data
     * */
    data class Accurate(override val value: Float) : QrVectorLogoPadding


    /**
     * Works like [Accurate] but all clipped pixels will be removed.
     *
     * This padding can also cause a little performance issues wen applied
     * to large-data qr codes, but not as much as [Accurate].
     * */
    data class Natural(override val value: Float) : QrVectorLogoPadding
}