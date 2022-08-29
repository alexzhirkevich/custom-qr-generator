package com.github.alexzhirkevich.customqrgenerator.style

import android.animation.ArgbEvaluator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnRepeat
import com.github.alexzhirkevich.customqrgenerator.QrDrawable
import kotlinx.coroutines.*
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlin.system.measureTimeMillis

enum class RepeatMode(val mode : Int){
    Reverse(ValueAnimator.REVERSE),
    Restart(ValueAnimator.RESTART),
}

private const val ANIMATION_THREAD_NAME = "animateQrColor"

/**
 * Create color animation from [colors].
 *
 * Used to animate colors in [QrDrawable].
 * If [QrDrawable.drawable] is displayed in [ImageView],
 * it should be invalidated on every change using [ImageView.invalidate].
 *
 * To set infinite [repeatCount] use -1
 * */
@JvmName("animateSolidColor")
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
suspend fun animateQrColor(
    colors : List<QrColor.Solid>,
    duration : Long = 500,
    cycleDelay: Long = 0,
    interpolator: TimeInterpolator = LinearInterpolator(),
    repeatMode : RepeatMode = RepeatMode.Restart,
    repeatCount : Int = 0,
    animationLooper : Looper = HandlerThread(ANIMATION_THREAD_NAME)
        .apply { start() }.looper,
    onChange : suspend (QrColor.Solid) -> Unit
) = animateQrColor(
    colors = colors.map(QrColor.Solid::color),
    duration = duration,
    cycleDelay = cycleDelay,
    interpolator = interpolator,
    repeatMode = repeatMode,
    repeatCount = repeatCount,
    animationLooper = animationLooper
) {
    onChange(QrColor.Solid(it))
}

/**
 * Create color animation from [colors].
 *
 * Used to animate colors in [QrDrawable].
 * If [QrDrawable.drawable] is displayed in [ImageView],
 * it should be invalidated on every change using [ImageView.invalidate].
 *
 * To set infinite [repeatCount] use -1
 * */
@JvmName("animateLinearGradientColor")
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
suspend fun animateQrColor(
    colors : List<QrColor.LinearGradient>,
    duration : Long = 500,
    cycleDelay: Long = 0,
    interpolator: TimeInterpolator = LinearInterpolator(),
    repeatMode : RepeatMode = RepeatMode.Restart,
    repeatCount : Int = 0,
    animationLooper : Looper = HandlerThread(ANIMATION_THREAD_NAME)
        .apply { start() }.looper,
    onChange : suspend (QrColor.LinearGradient) -> Unit
) = coroutineScope {

    if (colors.isEmpty())
        return@coroutineScope

    if (colors.size == 1){
        onChange(colors.first())
        return@coroutineScope
    }

    var startColor: Int = colors.first().startColor
    var endColor: Int = colors.first().endColor
    val orientation = colors.first().orientation

    launch {
        animateQrColor(
            colors = colors.map(QrColor.LinearGradient::startColor),
            duration = duration,
            cycleDelay = cycleDelay,
            interpolator = interpolator,
            repeatMode = repeatMode,
            repeatCount = repeatCount,
            animationLooper = animationLooper
        ) {
            startColor = it
            onChange(
                QrColor.LinearGradient(
                    startColor, endColor, orientation
                )
            )
        }
    }
    launch {
        animateQrColor(
            colors = colors.map(QrColor.LinearGradient::endColor),
            duration = duration,
            cycleDelay = cycleDelay,
            interpolator = interpolator,
            repeatMode = repeatMode,
            repeatCount = repeatCount,
            animationLooper = animationLooper
        ) {
            endColor = it
            onChange(
                QrColor.LinearGradient(
                    startColor, endColor, orientation
                )
            )
        }
    }
}

/**
 * Create color animation from [colors].
 *
 * Used to animate colors in [QrDrawable].
 * If [QrDrawable.drawable] is displayed in [ImageView],
 * it should be invalidated on every change using [ImageView.invalidate].
 *
 * Coroutine is suspended until animation end.
 * If animation is endless, coroutine will be suspended forever.
 *
 * @param duration animation cycle duration
 * @param cycleDelay delay between 2 animation cycles
 * @param interpolator animation interpolator
 * @param repeatMode [RepeatMode.Restart] or [RepeatMode.Reverse].
 * To set non-repeatable animation use [repeatCount] = 0
 * @param repeatCount count of animation repeats.
 * @param animationLooper
 * Pass -1 to set infinite repeat count or 0 to play animation only 1 time
 * */
@JvmName("animateRadialGradientColor")
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
suspend fun animateQrColor(
    colors : List<QrColor.RadialGradient>,
    duration : Long = 500,
    cycleDelay: Long = 0,
    interpolator: TimeInterpolator = LinearInterpolator(),
    repeatMode : RepeatMode = RepeatMode.Restart,
    repeatCount : Int = 0,
    animationLooper : Looper = HandlerThread(ANIMATION_THREAD_NAME)
        .apply { start() }.looper,
    onChange : suspend (QrColor.RadialGradient) -> Unit
) = animateQrColor(
    colors = colors.map {
        QrColor.LinearGradient(
            it.startColor, it.endColor,
            QrColor.LinearGradient.Orientation.LeftDiagonal
        )
    },
    duration = duration,
    cycleDelay = cycleDelay,
    interpolator = interpolator,
    repeatMode = repeatMode,
    repeatCount = repeatCount,
    animationLooper = animationLooper
) {
    onChange(QrColor.RadialGradient(it.startColor, it.endColor))
}

private suspend fun customAnimator(
    @ColorInt colors : List<Int>,
    duration : Long = 500,
    cycleDelay : Long = 0,
    interpolator: TimeInterpolator = LinearInterpolator(),
    repeatMode : RepeatMode = RepeatMode.Restart,
    repeatCount : Int = 0,
    animationFps : Int = 60,
    onChange : suspend (Int) -> Unit
) {
    if (colors.isEmpty())
        throw IllegalStateException("Cannot animate empty colors")
    var repeats = 0
    var isReversedCycle = false
    val frameTime = 1000/animationFps
    val evaluator = ArgbEvaluator()
    val cycleFrames = duration/frameTime
    do {
        repeat(cycleFrames.toInt()) {
            val elapsed = measureTimeMillis {
                val frac = it.toFloat() / cycleFrames
                val progress = interpolator.getInterpolation(
                        if (isReversedCycle)
                            1 - frac else frac
                    )
                val idx = (progress * (colors.lastIndex - 2)).toInt() + 1
                val (color1, color2) = when (colors.size) {
                    1 -> colors.first() to colors.first()
                    else -> colors[idx] to colors[idx + 1]
                }

                val currentColorProgress = progress * (colors.lastIndex - 1) / idx
                val color = evaluator.evaluate(currentColorProgress, color1, color2) as Int
                onChange(color)
            }
            delay(frameTime - elapsed)
        }
        repeats++
        if (repeatMode == RepeatMode.Reverse)
            isReversedCycle = !isReversedCycle
        delay(cycleDelay)
    } while (repeatCount < 0 || repeats < repeatCount)
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
private suspend fun animateQrColor(
    @ColorInt colors : List<Int>,
    duration : Long = 500,
    cycleDelay : Long = 0,
    interpolator: TimeInterpolator = LinearInterpolator(),
    repeatMode : RepeatMode = RepeatMode.Restart,
    repeatCount : Int = 0,
    animationLooper : Looper = HandlerThread(ANIMATION_THREAD_NAME)
        .apply { start() }.looper,
    onChange : suspend (Int) -> Unit
)
{
    customAnimator(colors,duration, cycleDelay, interpolator, repeatMode, repeatCount, 60,onChange)
}
//= callbackFlow {
//
//    val handler = Handler(animationLooper)
//    val dispatcher = handler.asCoroutineDispatcher()
//    val animator = ValueAnimator.ofArgb(
//        *colors.toIntArray()
//    ).apply {
//        this.duration = duration
//        this.repeatMode = repeatMode.mode
//        this.repeatCount = repeatCount
//        this.interpolator = interpolator
//        if (cycleDelay > 0) {
//            doOnRepeat {
//                pause()
//                launch(dispatcher) {
//                    delay(cycleDelay)
//                    resume()
//                }
//            }
//        }
//        doOnEnd {
//            this@callbackFlow.cancel()
//            with(animationLooper.thread){
//                if (name == ANIMATION_THREAD_NAME)
//                    (this as HandlerThread).quit()
//            }
//        }
//        addUpdateListener {
//            trySend(it.animatedValue as Int)
//        }
//    }
//
//    withContext(dispatcher) {
//        animator.start()
//    }
//    awaitClose {
//        handler.post {
//            animator.cancel()
//        }
//        with(animationLooper.thread){
//            if (name == ANIMATION_THREAD_NAME)
//                (this as HandlerThread).quit()
//        }
//    }
//}.collectLatest {
//    onChange(it)
//}
