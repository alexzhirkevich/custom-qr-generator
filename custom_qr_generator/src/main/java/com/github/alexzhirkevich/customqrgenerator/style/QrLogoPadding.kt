package com.github.alexzhirkevich.customqrgenerator.style

import com.github.alexzhirkevich.customqrgenerator.encoder.QrCodeMatrix

/**
 * Padding of the QR code logo
 * */
interface QrLogoPadding {

    /**
     * Logo padding relative to the logo size
     * */
    val value : Float

    /**
     * If it's true, [Accurate] padding will be applied on top of current padding.
     * Should be disabled if your padding is bigger than [Accurate] due to
     * optimization or if you just don't want to apply it
     * */
    val shouldApplyAccuratePadding : Boolean

    /**
     * Mark necessary pixels as [QrCodeMatrix.PixelType.Logo]
     *
     * This modifier will be applied before multiplying and shaping so
     * [logoSize] is given in QR pixels (not bitmap pixels) therefore it
     * is not accurate. [matrix] also is not scaled
     * */
    fun apply(matrix: QrCodeMatrix, logoSize: Int, logoPos : Int, logoShape: QrLogoShape)

    /**
     * Logo will be drawn on top of QR code. QR code might be visible through
     * transparent logo
     * */
    object Empty : QrLogoPadding {

        override val value: Float
            get() = 0f

        override val shouldApplyAccuratePadding: Boolean
            get() = false

        override fun apply(
            matrix: QrCodeMatrix,
            logoSize: Int,
            logoPos: Int,
            logoShape: QrLogoShape
        ) = Unit
    }

    /**
     * Padding will be applied directly according to the shape of logo.
     * Some QR code pixels can be cut
     * */
    class Accurate(override val value: Float) : QrLogoPadding {

        override val shouldApplyAccuratePadding: Boolean
            get() = true

        override fun apply(
            matrix: QrCodeMatrix,
            logoSize: Int,
            logoPos: Int,
            logoShape: QrLogoShape
        ) = Unit
    }

    /**
     * Works like [Accurate] but all clipped pixels will be removed.
     * Can be a little shifted on big data codes
     * */
    class Natural(override val value: Float) : QrLogoPadding {

        override val shouldApplyAccuratePadding: Boolean
            get() = false

        override fun apply(
            matrix: QrCodeMatrix,
            logoSize: Int,
            logoPos : Int,
            logoShape: QrLogoShape,
        ) {
            println(matrix.size)
            println(logoSize)
            println(logoPos)
            for (x in 0 until logoSize){
                for (y in 0 until logoSize){
                    if (logoShape.invoke(x, y, logoSize, Neighbors.Empty)){
                        matrix[logoPos+x, logoPos+y] =
                            QrCodeMatrix.PixelType.Logo
                    }
                }
            }
        }
    }
}