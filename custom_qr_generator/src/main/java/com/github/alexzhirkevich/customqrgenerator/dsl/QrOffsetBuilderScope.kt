package com.github.alexzhirkevich.customqrgenerator.dsl

import com.github.alexzhirkevich.customqrgenerator.QrOptions
import com.github.alexzhirkevich.customqrgenerator.style.IQrOffset
import com.github.alexzhirkevich.customqrgenerator.style.QrOffset

/**
 * @see QrOffset
 * */
interface QrOffsetBuilderScope : IQrOffset {
    override var x: Float
    override var y: Float
}

internal class InternalQrOffsetBuilderScope(
    private val builder: QrOptions.Builder
) : QrOffsetBuilderScope {
    override var x: Float
        get() = builder.offset.x
        set(value) = with(builder) {
            setOffset(offset.copy(x = value))
        }

    override var y: Float
        get() = builder.offset.y
        set(value) = with(builder) {
            setOffset(offset.copy(y = value))
        }

}