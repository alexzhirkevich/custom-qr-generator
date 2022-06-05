package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.github.alexzhirkevich.customqrgenerator.style.QrBackground
import com.github.alexzhirkevich.customqrgenerator.style.QrLogo
import com.github.alexzhirkevich.customqrgenerator.style.QrStyle
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

data class QrOptions(
    @IntRange(from = 0) val size : Int,
    @ColorInt val lightColor : Int,
    @ColorInt val darkColor : Int,
    @IntRange(from = 0) val padding : Int,
    val logo: QrLogo?,
    val background: QrBackground?,
    val style: QrStyle,
    val errorCorrectionLevel: ErrorCorrectionLevel
){
    class Builder(@IntRange(from = 0) private val size : Int){

        private var lightColor = Color.WHITE
        private var darkColor = Color.BLACK
        private var padding = size/16
        private var logo: QrLogo? = null
        private var background: QrBackground? = null
        private var style = QrStyle()
        private var errorCorrectionLevel = ErrorCorrectionLevel.L

        fun build() : QrOptions = QrOptions(
            size, lightColor, darkColor, padding, logo, background,style, errorCorrectionLevel
        )

        fun setLightColor(@ColorInt color : Int) : Builder {
            lightColor = color
            return this
        }

        fun setDarkColor(@ColorInt color : Int) : Builder {
            darkColor = color
            return this
        }

        /**
         * Padding of the QR code in bitmap pixels.
         * Must be less then [size]/2.
         * QR code with zero padding might be unreadable.
         * */
        fun setPadding(@IntRange(from = 0) padding: Int) : Builder {
            this.padding = padding
            return this
        }

        fun setLogo(logo : QrLogo?) : Builder {
            this.logo = logo
            return this
        }

        fun setBackground(background: QrBackground?) : Builder{
            this.background = background
            return this
        }

        fun setStyle(style: QrStyle) : Builder {
            this.style = style
            return this
        }

        /**
         * If you set too weak error correction level,
         * it will be increased automatically depending on logo size.
         * Stronger level requires more qr-pixels, but allows to have bigger logo.
         * */
        fun setErrorCorrectionLevel(level: ErrorCorrectionLevel){
            errorCorrectionLevel = level
        }
    }
}