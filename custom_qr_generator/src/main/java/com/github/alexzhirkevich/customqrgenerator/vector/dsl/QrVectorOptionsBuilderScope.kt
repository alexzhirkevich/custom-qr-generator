package com.github.alexzhirkevich.customqrgenerator.vector.dsl

import com.github.alexzhirkevich.customqrgenerator.QrErrorCorrectionLevel
import com.github.alexzhirkevich.customqrgenerator.dsl.*
import com.github.alexzhirkevich.customqrgenerator.dsl.InternalQrOffsetBuilderScope
import com.github.alexzhirkevich.customqrgenerator.style.QrShape
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions

sealed interface QrVectorOptionsBuilderScope  {

    var padding: Float
    var errorCorrectionLevel: QrErrorCorrectionLevel
    var codeShape : QrShape

    fun offset(block: QrOffsetBuilderScope.() -> Unit)
    fun shapes(block: QrVectorShapesBuilderScope.() -> Unit)
    fun colors(block: QrVectorColorsBuilderScope.() -> Unit)
    fun logo(block: QrVectorLogoBuilderScope.() -> Unit)
}

internal class InternalQrVectorOptionsBuilderScope(
    val builder: QrVectorOptions.Builder
) : QrVectorOptionsBuilderScope {

    override var padding: Float by builder::padding

    override var errorCorrectionLevel: QrErrorCorrectionLevel by builder::errorCorrectionLevel

    override var codeShape: QrShape by builder::shape

    override fun offset(block: QrOffsetBuilderScope.() -> Unit) {
        InternalQrOffsetBuilderScope(builder).apply(block)
    }

    override fun shapes(block: QrVectorShapesBuilderScope.() -> Unit) {
        InternalQrVectorShapesBuilderScope(builder).apply(block)
    }

    override fun colors(block: QrVectorColorsBuilderScope.() -> Unit) {
        InternalQrVectorColorsBuilderScope(builder).apply(block)
    }

    override fun logo(block: QrVectorLogoBuilderScope.() -> Unit) {
        InternalQrVectorLogoBuilderScope(builder)
            .apply(block)
    }
}