package com.github.alexzhirkevich.customqrgenerator.style

interface ModifierDelegate<T,M : QrShapeModifier<T>> : QrShapeModifier<T> {
    val delegate : QrShapeModifier<T>
}