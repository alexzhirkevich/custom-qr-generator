package com.github.alexzhirkevich.customqrgenerator.style

/**
 * Bitwise not
 * */
operator fun QrShapeModifier.not() : QrShapeModifier =
    QrShapeModifier { i, j, elementSize, neighbors ->
        invoke(i, j, elementSize, neighbors).not()
    }

/**
 * Bitwise or
 * */
fun QrShapeModifier.or(other: QrShapeModifier) : QrShapeModifier =
    QrShapeModifier { i, j, elementSize, neighbors ->
        invoke(i, j, elementSize, neighbors) || other(i, j, elementSize, neighbors)
    }

/**
 * Bitwise and
 * */
fun QrShapeModifier.and(other : QrShapeModifier): QrShapeModifier =
    QrShapeModifier { i, j, elementSize, neighbors ->
        invoke(i, j, elementSize, neighbors) && other(i, j, elementSize, neighbors)
    }


internal operator fun QrShapeModifier.rem(rem : Int) : QrShapeModifier =
    QrShapeModifier { i, j, _, neighbors ->
        invoke(i % rem, j % rem, rem, neighbors)
    }


internal operator fun QrShapeModifier.rem(
    remRuntime : (elemSize : Int, neighbors : Neighbors) -> Int
) : QrShapeModifier =
    QrShapeModifier { i, j, elementSize, neighbors ->
        val rem = remRuntime(elementSize, neighbors)
        invoke(i % rem, j % rem, rem, neighbors)
    }
