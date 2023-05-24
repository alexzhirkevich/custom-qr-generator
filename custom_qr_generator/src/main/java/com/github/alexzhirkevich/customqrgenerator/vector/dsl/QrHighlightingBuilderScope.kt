package com.github.alexzhirkevich.customqrgenerator.vector.dsl

import com.github.alexzhirkevich.customqrgenerator.HighlightingType
import com.github.alexzhirkevich.customqrgenerator.IAnchorsHighlighting

sealed interface QrHighlightingBuilderScope : IAnchorsHighlighting {
    override var cornerEyes: HighlightingType
    override var versionEyes: HighlightingType
    override var timingLines: HighlightingType
    override val alpha: Float
}