package com.github.alexzhirkevich.customqrgenerator.style

import android.graphics.Path
import androidx.core.graphics.and
import java.util.concurrent.ConcurrentHashMap

class QrShapeModifierFromPath(
    private val path: Path.(Int) -> Unit
) : QrShapeModifier {

    private val cache: MutableMap<Int, Path> = ConcurrentHashMap()

    override fun invoke(
        i: Int, j: Int, elementSize: Int, neighbors: Neighbors
    ): Boolean {
        val mPath = cache[elementSize] ?: Path().apply {
            path(elementSize)
            cache[elementSize] = this
        }
        val point = Path().apply {
            addRect(
                i.toFloat(), j.toFloat(),
                i + .49f, j + .49f,
                Path.Direction.CW
            )
        }
        return mPath.and(point).isEmpty.not()
    }
}

