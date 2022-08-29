package com.github.alexzhirkevich.customqrgenerator

import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalSerializationApi
val QrSerializersModule by lazy(LazyThreadSafetyMode.NONE) {
    SerializersModuleFromProviders(QrOptions, QrData)
}