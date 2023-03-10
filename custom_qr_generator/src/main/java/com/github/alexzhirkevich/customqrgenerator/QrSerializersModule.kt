@file:Suppress("UNUSED")

package com.github.alexzhirkevich.customqrgenerator

import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalSerializationApi
val QrSerializersModule by lazy(LazyThreadSafetyMode.NONE) {
    SerializersModuleFromProviders(QrOptions, QrVectorOptions, QrData)
}