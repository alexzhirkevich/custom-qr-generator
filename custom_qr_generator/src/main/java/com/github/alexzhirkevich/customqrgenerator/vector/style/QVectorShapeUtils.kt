package com.github.alexzhirkevich.customqrgenerator.vector.style

import android.graphics.Path
import androidx.core.graphics.scaleMatrix
import androidx.core.graphics.translationMatrix
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors

fun QrVectorShapeModifier.createPath(size : Float, neighbors: Neighbors) : Path = Path().apply {
    shape(size, neighbors)
}

inline infix operator fun QrVectorShapeModifier.plus(other : QrVectorShapeModifier) : QrVectorShapeModifier =
    op(other, Path.Op.UNION)

inline infix operator fun QrVectorShapeModifier.minus(other : QrVectorShapeModifier) : QrVectorShapeModifier =
    op(other, Path.Op.DIFFERENCE)

inline infix fun QrVectorShapeModifier.or(other : QrVectorShapeModifier) : QrVectorShapeModifier =
    plus(other)
inline infix fun QrVectorShapeModifier.xor(other : QrVectorShapeModifier) : QrVectorShapeModifier =
    op(other, Path.Op.XOR)

inline infix fun QrVectorShapeModifier.and(other : QrVectorShapeModifier) : QrVectorShapeModifier =
    op(other, Path.Op.INTERSECT)

fun QrVectorShapeModifier.op(other: QrVectorShapeModifier, op : Path.Op) =
    QrVectorShapeModifier { size, neighbors ->
        this@op.run {
            shape(size, neighbors)
        }
        op(Path().apply {
            other.run {
                shape(size, neighbors)
            }
        }, op)
    }

fun QrVectorShapeModifier.asFrameShape() : QrVectorFrameShape =
    object : QrVectorFrameShape, QrVectorShapeModifier by this {}

fun QrVectorShapeModifier.asBallShape() : QrVectorBallShape =
    object : QrVectorBallShape, QrVectorShapeModifier by this {}

fun QrVectorShapeModifier.asPixelShape() : QrVectorPixelShape =
    object : QrVectorPixelShape, QrVectorShapeModifier by this {}

fun QrVectorShapeModifier.asLogoShape() : QrVectorLogoShape =
    object : QrVectorLogoShape, QrVectorShapeModifier by this {}

/**
 * Scale shape with center pivot
 * */
fun QrVectorShapeModifier.scale(scaleX : Float, scaleY: Float = scaleX) : QrVectorShapeModifier =
    QrVectorShapeModifier {s, n ->
        shape(s,n)
        transform(scaleMatrix(scaleX,scaleY), this)
        transform(translationMatrix(s * (1 - scaleX)/2, s * (1 - scaleY)/2))
    }