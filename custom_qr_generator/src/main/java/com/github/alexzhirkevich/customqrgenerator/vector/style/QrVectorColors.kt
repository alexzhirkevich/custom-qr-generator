package com.github.alexzhirkevich.customqrgenerator.vector.style

import com.github.alexzhirkevich.customqrgenerator.style.toColor

interface IQrVectorColors {
    val dark : QrVectorColor
    val light : QrVectorColor
    val ball : QrVectorColor
    val frame : QrVectorColor
}

data class QrVectorColors(
    override val dark : QrVectorColor = QrVectorColor.Solid(0xff000000.toColor()),
    override val light : QrVectorColor = QrVectorColor.Solid(0),
    override val ball : QrVectorColor = QrVectorColor.Unspecified,
    override val frame : QrVectorColor = QrVectorColor.Unspecified
) : IQrVectorColors