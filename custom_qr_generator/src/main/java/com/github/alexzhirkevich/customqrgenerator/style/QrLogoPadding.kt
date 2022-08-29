package com.github.alexzhirkevich.customqrgenerator.style

import com.github.alexzhirkevich.customqrgenerator.SerializationProvider
import com.github.alexzhirkevich.customqrgenerator.encoder.QrCodeMatrix
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

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
    @Serializable
    @SerialName("Empty")
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
    @Serializable
    @SerialName("Accurate")
    data class Accurate(override val value: Float) : QrLogoPadding {

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
    @Serializable
    @SerialName("Natural")
    data class Natural(override val value: Float) : QrLogoPadding {

        override val shouldApplyAccuratePadding: Boolean
            get() = false

        override fun apply(
            matrix: QrCodeMatrix,
            logoSize: Int,
            logoPos : Int,
            logoShape: QrLogoShape,
        ) {
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

    companion object : SerializationProvider {

        @ExperimentalSerializationApi
        @Suppress("unchecked_cast")
        override val defaultSerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModule {
                polymorphicDefaultSerializer(QrLogoPadding::class){
                    Empty.serializer() as SerializationStrategy<QrLogoPadding>
                }
                polymorphicDefaultDeserializer(QrLogoPadding::class) {
                    Empty.serializer()
                }
                polymorphic(QrLogoPadding::class) {
                    subclass(Empty::class)
                    subclass(Accurate::class)
                    subclass(Natural::class)
                }
            }
        }
    }
}