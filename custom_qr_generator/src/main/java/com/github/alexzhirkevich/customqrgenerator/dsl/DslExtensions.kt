package com.github.alexzhirkevich.customqrgenerator.dsl

import android.graphics.Canvas
import android.graphics.Paint
import com.github.alexzhirkevich.customqrgenerator.QrOptions
import com.github.alexzhirkevich.customqrgenerator.style.*
import kotlin.math.roundToInt
import kotlin.reflect.KClass


/**
 * Draw anything you want on your QR code.
 * And make sure it is able to scan.
 *
 * @return color linked to built [QrOptions].
 * Should not be used for [QrOptions] with changed [QrOptions.size] or
 * [QrOptions.padding] * */
inline fun QrColorsBuilderScope.draw(
    crossinline action : Canvas.() -> Unit
) : QrColor = QrCanvasColor { canvas -> action(canvas) }
    .let {
        val (width, height)=when(this){
            is InternalColorsBuilderScope -> builder.width to builder.height
        }
        it.toQrColor(width, height)
    }



/**
 * Create a custom [QrElementsShapes] properties by drawing on [Canvas].
 * And make sure it is able to scan.

 * @return [T] shape modifier linked to built [QrOptions].
 * Should not be used for other [QrOptions]
 * */
inline fun <reified T : QrShapeModifier> QrElementsShapesBuilderScope.drawShape(
    noinline draw : (canvas : Canvas, drawPaint : Paint, erasePaint : Paint) -> Unit
): T = drawShape(T::class, draw)


/**
 * Create a custom [QrElementsShapes] properties by drawing on [Canvas].
 * And make sure it is able to scan.

 * @return [T] shape modifier linked to built [QrOptions].
 * Should not be used for other [QrOptions]
 * */
inline fun <reified T : QrShapeModifier> QrLogoBuilderScope.drawShape(
    noinline draw : (canvas : Canvas, drawPaint : Paint, erasePaint : Paint) -> Unit
): T = drawShape(T::class, draw)


/**
 * @see [drawShape]
 */
@Suppress("unchecked_cast")
fun <T : QrShapeModifier> QrElementsShapesBuilderScope.drawShape(
    clazz: KClass<T>,
    draw : (canvas : Canvas, drawPaint : Paint, erasePaint : Paint) -> Unit
) : T = QrCanvasShape(draw)
    .let {
        val (size, padding) = when (this) {
            is InternalQrElementsShapesBuilderScope ->
                minOf(builder.width, builder.height) to builder.padding
        }
        it.toTypedShapeModifier(clazz, size, padding)
    }

/**
 * @see [drawShape]
 */
@Suppress("unchecked_cast")
fun <T : QrShapeModifier> QrLogoBuilderScope.drawShape(
    clazz: KClass<T>,
    draw : (canvas : Canvas, drawPaint : Paint, erasePaint : Paint) -> Unit
) : T = QrCanvasShape(draw)
    .let {
        val (size, padding) = when (this) {
            is InternalQrLogoBuilderScope ->
                minOf(builder.width, builder.height) to builder.padding
        }
        it.toTypedShapeModifier(clazz, size, padding)
    }

@Suppress("unchecked_cast")
private fun <T : QrShapeModifier> QrCanvasShape.toTypedShapeModifier(
    clazz: KClass<T>,
    size: Int,
    padding : Float,
) : T = when (clazz) {
    QrPixelShape::class -> toShapeModifier((size * (1 - padding) / 21).roundToInt())
        .asPixelShape()
    QrBallShape::class -> toShapeModifier((size * (1 - padding) / 7).roundToInt())
        .asBallShape()
    QrFrameShape::class -> toShapeModifier((size * (1 - padding) / 3).roundToInt())
        .asFrameShape()
    QrLogoShape::class -> toShapeModifier((size * (1 - padding) / 3).roundToInt())
        .asLogoShape()
    QrHighlightingShape::class -> toShapeModifier((size * (1 - padding) / 3).roundToInt())
        .asHighlightingShape()
    else -> throw IllegalStateException(
        "Only QrElementsShapes properties and QrLogoShape can be created via drawShape function"
    )
} as T
