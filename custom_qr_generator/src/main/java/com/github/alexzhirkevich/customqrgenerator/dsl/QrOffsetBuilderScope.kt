package com.github.alexzhirkevich.customqrgenerator.dsl

import com.github.alexzhirkevich.customqrgenerator.style.IQrOffset
import com.github.alexzhirkevich.customqrgenerator.style.QrOffset
import com.github.alexzhirkevich.customqrgenerator.style.QrOffsetBuilder

/**
 * @see QrOffset
 * */
sealed interface QrOffsetBuilderScope : IQrOffset {
    override var x: Float
    override var y: Float
}

internal class InternalQrOffsetBuilderScope(
    private val builder: QrOffsetBuilder
) : QrOffsetBuilderScope {

    override var x: Float
        get() = builder.offset.x
        set(value) = with(builder) {
            offset = offset.copy(x = value)
        }

    override var y: Float
        get() = builder.offset.y
        set(value) = with(builder) {
            offset = offset.copy(y = value)
        }
}
