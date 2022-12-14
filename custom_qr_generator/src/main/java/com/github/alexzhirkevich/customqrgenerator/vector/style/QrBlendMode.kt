package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.PorterDuff

enum class QrBlendMode(val actual : PorterDuff.Mode) {
    Normal(PorterDuff.Mode.SRC),
    SrcIn(PorterDuff.Mode.SRC_IN)
}