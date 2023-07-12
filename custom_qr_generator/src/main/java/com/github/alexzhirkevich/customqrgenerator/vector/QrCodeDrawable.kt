package com.github.alexzhirkevich.customqrgenerator.vector

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.*
import com.github.alexzhirkevich.customqrgenerator.HighlightingType
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.QrErrorCorrectionLevel
import com.github.alexzhirkevich.customqrgenerator.color
import com.github.alexzhirkevich.customqrgenerator.elementShape
import com.github.alexzhirkevich.customqrgenerator.encoder.QrCodeMatrix
import com.github.alexzhirkevich.customqrgenerator.encoder.neighbors
import com.github.alexzhirkevich.customqrgenerator.encoder.toQrMatrix
import com.github.alexzhirkevich.customqrgenerator.fit
import com.github.alexzhirkevich.customqrgenerator.shape
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.style.QrShape
import com.github.alexzhirkevich.customqrgenerator.style.forEyeWithNumber
import com.github.alexzhirkevich.customqrgenerator.vector.style.*
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.encoder.Encoder
import java.nio.charset.Charset
import kotlin.math.roundToInt

/**
 * Vector QR code image
 * */
fun QrCodeDrawable(
    data: QrData,
    options: QrVectorOptions = QrVectorOptions.Builder().build(),
    charset: Charset ?= null
) : Drawable = QrCodeDrawableImpl(data, options, charset)


private class QrCodeDrawableImpl(
    data: QrData,
    private val options: QrVectorOptions,
    charset: Charset?=null
) : Drawable() {

    private var anchorCenters: List<Pair<Int, Int>>

    private val initialMatrix: QrCodeMatrix

    private val shapeIncrease: Int

    init {
        val code = Encoder.encode(
            data.encode(),
            with(options.errorCorrectionLevel) {
                if (this == QrErrorCorrectionLevel.Auto)
                    fit(options.logo, options.codeShape).lvl
                else lvl
            },
            charset?.let {
                mapOf(EncodeHintType.CHARACTER_SET to it)
            })

        initialMatrix = code.matrix.toQrMatrix().apply {
            if (options.fourthEyeEnabled) {
                for (i in size - 8 until size) {
                    for (j in size - 8 until size) {
                        this[i, j] = QrCodeMatrix.PixelType.Background
                    }
                }
            }
        }


        shapeIncrease = (((initialMatrix.size * options.codeShape.shapeSizeIncrease) - initialMatrix.size) / 2).roundToInt()

        val (max, min) = code.version.alignmentPatternCenters.let {
            (it.maxOrNull() ?: 0) to (it.minOrNull() ?: 0)
        }

        anchorCenters = code.version.alignmentPatternCenters
            .toList().pairCombinations().filterNot {
                it.first == min && it.second == min ||
                        it.first == max && it.second == min ||
                        it.first == min && it.second == max ||
                        options.fourthEyeEnabled && it.first == max && it.second == max
            }

        if (options.highlighting.versionEyes.elementShape != null) {
            initialMatrix.apply {
                anchorCenters.forEach {
                    for (x in it.first - 2 until it.first + 3) {
                        for (y in it.second - 2 until it.second + 3) {
                            this[x, y] = QrCodeMatrix.PixelType.VersionEye
                        }
                    }
                }
            }
        }
    }

    private val shouldSeparateFrames
        get() = options.colors.frame.isSpecified || shouldSeparateDarkPixels
    private val shouldSeparateBalls
        get() = options.colors.ball.isSpecified || shouldSeparateDarkPixels

    private val shouldSeparateDarkPixels
        get() = options.colors.dark.mode == QrPaintMode.Separate

    private val shouldSeparateLightPixels
        get() = options.colors.light.mode == QrPaintMode.Separate

    private val codeMatrix = options.codeShape.apply(initialMatrix)

    private val balls = mutableListOf(
        2 + shapeIncrease to 2 + shapeIncrease,
        codeMatrix.size - 5 - shapeIncrease to 2 + shapeIncrease,
        2 + shapeIncrease to codeMatrix.size - 5 - shapeIncrease,
    ).apply {
        if (options.fourthEyeEnabled)
            this += codeMatrix.size - 5 - shapeIncrease to codeMatrix.size - 5 - shapeIncrease
    }.toList()

    private val frames = mutableListOf(
        shapeIncrease to shapeIncrease,
        codeMatrix.size - 7 - shapeIncrease to shapeIncrease,
        shapeIncrease to codeMatrix.size - 7 - shapeIncrease,
    ).apply {
        if (options.fourthEyeEnabled) {
            this += codeMatrix.size - 7 - shapeIncrease to codeMatrix.size - 7 - shapeIncrease
        }
    }.toList()

    private var mColorFilter: ColorFilter? = null
    private var mAlpha = 255


    private var bitmap: Bitmap? = null
    override fun setAlpha(alpha: Int) {
        mAlpha = alpha
    }


    override fun setColorFilter(colorFilter: ColorFilter?) {
        mColorFilter = colorFilter
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setBounds(bounds: Rect) {
        setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom)
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        resize(right - left, bottom - top)
    }

    private fun framePaintFactory(pixelSize: Float): Lazy<Paint> {

        val color = options.colors.frame
            .takeIf { it.isSpecified }
            ?: options.colors.dark

        val paint = Paint()

        var number = 0

        val paintFactory = {
            paint.reset()
            color.run {
                paint.paint(
                    width = pixelSize * 7f,
                    height = pixelSize * 7f,
                    neighbors = Neighbors.forEyeWithNumber(number, options.fourthEyeEnabled)
                )
            }
            number = (number+1)  % if (options.fourthEyeEnabled) 4 else 3
            paint
        }

        return Recreating(paintFactory)
    }

    private fun ballPaintFactory(pixelSize: Float): Lazy<Paint> {

        val color = options.colors.ball
            .takeIf { it.isSpecified }
            ?: options.colors.dark

        val paint = Paint()
        var number = 0

        val paintFactory = {
            paint.reset()
            color.run {
                paint.paint(
                    width = pixelSize * 3f,
                    height = pixelSize * 3f,
                    neighbors = Neighbors
                        .forEyeWithNumber(number, options.fourthEyeEnabled)
                )
            }
            number = (number+1)  % if (options.fourthEyeEnabled) 4 else 3
            paint
        }

        return Recreating(paintFactory)
    }

    private fun darkPaintFactory(pixelSize: Float): PixelPaintFactory {

        val paint = Paint()

        val size = if (shouldSeparateDarkPixels)
            pixelSize else codeMatrix.size * pixelSize

        val paintFactory = { n: Neighbors ->
            paint.reset()
            options.colors.dark.run {
                paint.paint(
                    width = size,
                    height = size,
                    neighbors = n
                )
            }
            paint
        }

        val lazy by lazy { paintFactory(Neighbors.Empty) }

        return PixelPaintFactory {
            if (shouldSeparateDarkPixels)
                paintFactory(it)
            else lazy
        }
    }

    private fun rotatedBallPath(pixelSize: Float): Lazy<Path> {

        val path = Path()

        val pathFactory = PixelPathFactory {
            path.rewind()
            options.shapes.ball.run {
                path.shape(pixelSize * 3f, it)
            }
            path
        }

        var number = -1

        return Recreating {

            number = (number + 1) % if (options.fourthEyeEnabled) 4 else 3


            pathFactory.next(
                neighbors = Neighbors.forEyeWithNumber(
                    number = number,
                    fourthEyeEnabled = options.fourthEyeEnabled
                )
            ).apply {
                if (options.shapes.centralSymmetry) {
                    val angle = when (number) {
                        0 -> 0f
                        1 -> 90f
                        2 -> -90f
                        else -> 180f
                    }
                    transform(
                        rotationMatrix(
                            angle,
                            pixelSize * 3 / 2,
                            pixelSize * 3 / 2
                        )
                    )
                }
            }
        }
    }

    private fun rotatedFramePath(pixelSize: Float): Lazy<Path> {

        var number = -1

        val path = Path()

        val pathFactory = PixelPathFactory {
            path.rewind()
            options.shapes.frame.run {
                path.shape(pixelSize * 7f, it)
            }
            path
        }

        return Recreating {
            number = (number + 1) % if (options.fourthEyeEnabled) 4 else 3

            pathFactory.next(Neighbors.forEyeWithNumber(number, options.fourthEyeEnabled)).apply {
                if (options.shapes.centralSymmetry) {
                    val angle = when (number) {
                        0 -> 0f
                        1 -> 90f
                        2 -> -90f
                        else -> 180f
                    }
                    transform(
                        rotationMatrix(
                            angle,
                            pixelSize * 7 / 2,
                            pixelSize * 7 / 2
                        )
                    )
                }
            }
        }
    }

    private fun lightPathFactory(pixelSize: Float): PixelPathFactory {
        val path = Path()

        return PixelPathFactory {
            path.rewind()
            options.shapes.lightPixel.run {
                path.shape(pixelSize, it)
            }
            path
        }
    }

    private fun lightPaintFactory(pixelSize: Float): PixelPaintFactory {

        val paint = Paint()

        val size = if (shouldSeparateLightPixels)
            pixelSize else codeMatrix.size * pixelSize

        val paintFactory = { n : Neighbors ->
            paint.reset()

            options.colors.light.run {
                paint.paint(width = size, height = size, neighbors = n)
            }
            paint
        }

        val lazy by lazy { paintFactory(Neighbors.Empty) }

        return PixelPaintFactory {
            if (shouldSeparateLightPixels)
                paintFactory(it)
            else lazy
        }
    }

    private fun darkPathFactory(pixelSize: Float): PixelPathFactory {
        val path = Path()
        return PixelPathFactory {
            path.rewind()
            options.shapes.darkPixel.run {
                path.shape(pixelSize, it)
            }
            path
        }
    }


    private fun Canvas.drawBg(background: Bitmap?) {
        if (options.background.color !is QrVectorColor.Unspecified &&
            options.background.color !is QrVectorColor.Transparent
        ) {
            drawPaint(
                options.background.color.createPaint(
                    width = bounds.width().toFloat(),
                    height = bounds.height().toFloat(),
                    neighbors = Neighbors.Empty
                )
            )
        }

        background?.let {
            drawBitmap(it, 0f, 0f, null)
        }
    }

    private fun Canvas.drawFrames(
        pixelSize: Float
    ) {
        val framePaint by framePaintFactory(pixelSize)
        val framePath by rotatedFramePath(pixelSize)

        frames.forEach {
            withTranslation(
                it.first * pixelSize,
                it.second * pixelSize
            ) {
                drawPath(framePath, framePaint)
            }
        }
    }

    private fun Canvas.drawBalls(pixelSize: Float) {

        val ballPaint by ballPaintFactory(pixelSize)
        val ballPath by rotatedBallPath(pixelSize)

        balls.forEach {

            withTranslation(
                it.first * pixelSize,
                it.second * pixelSize
            ) {
                drawPath(ballPath, ballPaint)
            }
        }
    }

    private fun createHighlightingPaint(
        highlightingType: HighlightingType,
        size: Float
    ): Paint {
        val color = highlightingType.color ?: DefaultHighlightingColor

        return color
            .createPaint(size, size, Neighbors.Empty)
    }

    private fun Canvas.highlightVersionEyesIfNeeded(pixelSize: Float) {
        val (shape, paint) = when (options.highlighting.versionEyes) {
            HighlightingType.None -> return
            HighlightingType.Default -> DefaultVersionFrame to DefaultHighlightedElementColor
            is HighlightingType.Styled ->
                (options.highlighting.versionEyes.elementShape ?: DefaultVersionFrame) to
                        options.highlighting.versionEyes.elementColor
        }

        val highlightPaint = createHighlightingPaint(
            options.highlighting.versionEyes, pixelSize * 5
        )

        val highlightShape = options.highlighting.versionEyes.shape
            .createPath(pixelSize * 5, Neighbors.Empty)

        val elSize = pixelSize * 5
        anchorCenters.forEach {
            withTranslation(
                (shapeIncrease + it.first - 2) * pixelSize,
                (shapeIncrease + it.second - 2) * pixelSize
            ) {

                drawPath(highlightShape, highlightPaint)
                drawPath(
                    shape.createPath(elSize, Neighbors.Empty),
                    paint.createPaint(elSize, elSize, Neighbors.Empty)
                )
            }
        }
    }

    private fun Canvas.highlightCornerEyesIfNeed(pixelSize: Float) {

        val shape = when (options.highlighting.cornerEyes) {
            HighlightingType.None -> return
            HighlightingType.Default -> QrVectorBallShape.Default
                .createPath(pixelSize * 9, Neighbors.Empty)

            is HighlightingType.Styled -> options.highlighting.cornerEyes.shape
                .createPath(pixelSize * 9, Neighbors.Empty)

        }

        val paint = createHighlightingPaint(options.highlighting.cornerEyes, pixelSize * 9)

        frames.forEach {
            withTranslation(
                (it.first - 1) * pixelSize,
                (it.second - 1) * pixelSize
            ) {
                drawPath(shape, paint)
            }
        }
    }

    private fun Canvas.highlightTimingLinesIfNeed(pixelSize: Float) {

        repeat(2) { idx ->

            for (i in 8+ shapeIncrease  until codeMatrix.size - 8 - shapeIncrease) {

                val (x, y) = listOf(i, 6 + shapeIncrease).let { if (idx == 0) it else it.reversed() }

                if (isInsideVersionEye(x,y) && options.highlighting.versionEyes !is HighlightingType.None)
                    continue

                val path = when (options.highlighting.timingLines) {
                    HighlightingType.None -> continue

                    HighlightingType.Default -> DefaultTimingLinePixel
                        .createPath(pixelSize, Neighbors.Empty)

                    is HighlightingType.Styled -> {
                        if (codeMatrix[x, y] == QrCodeMatrix.PixelType.DarkPixel) {
                            (options.highlighting.timingLines.elementShape ?: DefaultTimingLinePixel)
                                .createPath(pixelSize, Neighbors.Empty)
                        } else {
                            options.highlighting.timingLines.shape
                                .createPath(pixelSize, Neighbors.Empty)
                        }
                    }
                }

                val paint = when (options.highlighting.timingLines) {
                    HighlightingType.None -> continue

                    HighlightingType.Default -> (if (codeMatrix[x, y] == QrCodeMatrix.PixelType.DarkPixel)
                        DefaultHighlightedElementColor else DefaultHighlightingColor)
                        .createPaint(
                            width = pixelSize,
                            height = pixelSize,
                            neighbors = Neighbors.Empty
                        )

                    is HighlightingType.Styled -> if (codeMatrix[x, y] == QrCodeMatrix.PixelType.DarkPixel) {
                        options.highlighting.timingLines.elementColor
                            .createPaint(
                                width = pixelSize,
                                height = pixelSize,
                                neighbors = Neighbors.Empty
                            )
                    } else {
                        options.highlighting.timingLines.color
                            .createPaint(
                                width = pixelSize,
                                height = pixelSize,
                                neighbors = Neighbors.Empty
                            )
                    }
                }

                withTranslation(x * pixelSize, y * pixelSize) {
                    drawPath(path, paint)
                }
            }
        }
    }

    override fun draw(canvas: Canvas) {
        bitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }

    private fun drawToCanvas(
        canvas: Canvas,
        size: Float,
        pixelSize: Float,
        darkPixelPath: Path,
        lightPixelPath: Path,
        logoBgSize: Float,
        logoBgPath: Path,
        logoBgPaint: Paint?,
        logo: Bitmap?,
        background: Bitmap?
    ) {

        val (w, h) = bounds.width() to bounds.height()

        val (offsetX, offsetY) = with(options.offset) { listOf(x, y) }
            .map { it.coerceIn(-1f, 1f) + 1 }

        val density = canvas.density
        canvas.density = Bitmap.DENSITY_NONE

        val darkPixelPaint = darkPaintFactory(pixelSize)
        val lightPixelPaint = lightPaintFactory(pixelSize)

        canvas.drawBg(background)

//        options.background.drawable?.let {
//            it.draw(canvas)
//        }

        canvas.withTranslation(
            (w - size) / 2f * offsetX,
            (h - size) / 2f * offsetY
        ) {

            canvas.highlightCornerEyesIfNeed(pixelSize)
            canvas.highlightTimingLinesIfNeed(pixelSize)
            canvas.highlightVersionEyesIfNeeded(pixelSize)

            if (shouldSeparateDarkPixels || shouldSeparateLightPixels) {

                val darkPath = darkPathFactory(pixelSize)
                val lightPath = lightPathFactory(pixelSize)

                repeat(codeMatrix.size) { i ->
                    repeat(codeMatrix.size) { j ->
                        if (isOnTimingLine(i, j) && options.highlighting.timingLines !is HighlightingType.None)
                            return@repeat

                        if (isInsideFrameOrBall(i,j))
                            return@repeat

                        withTranslation(
                            x = i * pixelSize,
                            y = j * pixelSize
                        ) {
                            if (shouldSeparateDarkPixels && codeMatrix[i, j] == QrCodeMatrix.PixelType.DarkPixel) {
                                val n = codeMatrix.neighbors(i, j)
                                drawPath(
                                    darkPath.next(n),
                                    darkPixelPaint.next(n)
                                )
                            }
                            if (shouldSeparateLightPixels && codeMatrix[i, j] == QrCodeMatrix.PixelType.LightPixel) {
                                val n = codeMatrix.neighbors(i, j)

                                drawPath(
                                    lightPath.next(n),
                                    lightPixelPaint.next(n)
                                )
                            }
                        }
                    }
                }
            }

            if (!shouldSeparateDarkPixels) {
                drawPath(darkPixelPath, darkPixelPaint.next(Neighbors.Empty))
            }
            if (!shouldSeparateLightPixels) {
                drawPath(lightPixelPath, lightPixelPaint.next(Neighbors.Empty))
            }

//            drawPath(
//                darkTimingPath, when (options.highlighting.timingLines) {
//                    HighlightingType.Default -> QrVectorColor.Solid(Color.BLACK)
//                        .createPaint(
//                            width = (codeMatrix.size-12) * pixelSize,
//                            height = (codeMatrix.size-12) * pixelSize,
//                            neighbors = Neighbors.Empty
//                        )
//
//                    HighlightingType.None -> Paint() // path is empty
//                    is HighlightingType.Styled -> darkPixelPaint.next(Neighbors.Empty)
//                }
//            )
//
//            drawPath(lightTimingPath, when (options.highlighting.timingLines) {
//                HighlightingType.Default -> QrVectorColor.Solid(Color.WHITE)
//                    .createPaint(
//                        width = (codeMatrix.size-12) * pixelSize,
//                        height = (codeMatrix.size-12) * pixelSize,
//                        neighbors = Neighbors.Empty
//                    ).apply {
//                        alpha = (options.highlighting.alpha.coerceIn(0f, 1f) * 255).roundToInt()
//                    }
//
//                HighlightingType.None -> Paint() // path is empty
//                is HighlightingType.Styled -> options.highlighting.timingLines.color?.createPaint(
//                    width = codeMatrix.size * pixelSize,
//                    height = codeMatrix.size * pixelSize,
//                    neighbors = Neighbors.Empty
//                ) ?: lightPixelPaint.next(Neighbors.Empty)
//            })


            if (shouldSeparateFrames) {
                drawFrames(pixelSize)
            }

            if (shouldSeparateBalls) {
                drawBalls(pixelSize)
            }

            val (x, y) = (size - logoBgSize) / 2f to (size - logoBgSize) / 2f

            if (logoBgPaint != null)
                withTranslation(x, y) {
                    drawPath(logoBgPath, logoBgPaint)
                }
//
            if (logo != null) {
                val (x2, y2) = (size - logo.width) / 2f to (size - logo.height) / 2f
                drawBitmap(logo, x2, y2, null)
            }
        }
        canvas.density = density
    }


    private fun applyNaturalLogo(
        logoBgSize: Int,
        size: Float,
        pixelSize: Float
    ) {
        val bgPath1 = options.logo.shape.createPath(
            size = logoBgSize.toFloat(),
            neighbors = Neighbors.Empty
        ).apply {
            transform(
                translationMatrix(
                    (size - logoBgSize) / 2f,
                    (size - logoBgSize) / 2f,
                )
            )
        }

        val darkPathF = darkPathFactory(pixelSize)
        val lightPathF = lightPathFactory(pixelSize)

        val logoPixels = (codeMatrix.size *
                options.logo.size.coerceIn(0f,1f) *
                (1 + options.logo.padding.value.coerceIn(0f,1f))).roundToInt() + 1

        for (x in (codeMatrix.size - logoPixels)/2 until (codeMatrix.size + logoPixels)/2) {
            for (y in (codeMatrix.size - logoPixels)/2 until (codeMatrix.size + logoPixels)/2) {

                val neighbors = codeMatrix.neighbors(x, y)
                val darkPath = darkPathF.next(neighbors)
                val lightPath = lightPathF.next(neighbors)

                if (codeMatrix[x, y] == QrCodeMatrix.PixelType.DarkPixel &&
                    bgPath1.and(darkPath.apply {
                        transform(
                            translationMatrix(
                                x * pixelSize,
                                y * pixelSize
                            )
                        )
                    }).isEmpty.not() ||
                    (codeMatrix[x, y] == QrCodeMatrix.PixelType.LightPixel &&
                            bgPath1.and(
                                lightPath.apply {
                                    transform(
                                        translationMatrix(
                                            x * pixelSize,
                                            y * pixelSize
                                        )
                                    )
                                }).isEmpty.not()
                            )
                ) {
                    codeMatrix[x, y] = QrCodeMatrix.PixelType.Logo
                }
            }
        }
    }

    private fun isOnTimingLine(x: Int, y: Int) =
        x in shapeIncrease..codeMatrix.size-shapeIncrease
                && y in shapeIncrease..codeMatrix.size-shapeIncrease
                && (x - shapeIncrease == 6 || y - shapeIncrease == 6)
                && !isInsideFrameOrBall(x, y)

    private fun isVersionEyeCenter(x: Int, y: Int) =
        anchorCenters.any { it.first == x - shapeIncrease && it.second == y - shapeIncrease }

    private fun isInsideVersionEye(x: Int, y: Int) =
        anchorCenters.any { x - shapeIncrease in it.first-2..it.first+2 &&
                y - shapeIncrease in it.second-2..it.second+2 }


    private fun isFrameStart(x: Int, y: Int) =
        x - shapeIncrease == 0 && y - shapeIncrease == 0 ||
                x - shapeIncrease == 0 && y + shapeIncrease == codeMatrix.size - 7 ||
                x + shapeIncrease == codeMatrix.size - 7 && y - shapeIncrease == 0 ||
                options.fourthEyeEnabled && x + shapeIncrease == codeMatrix.size - 7 && y + shapeIncrease == codeMatrix.size - 7

    private fun isBallStart(x: Int, y: Int) =
        x - shapeIncrease == 2 && y + shapeIncrease == codeMatrix.size - 5 ||
                x + shapeIncrease == codeMatrix.size - 5 && y - shapeIncrease == 2 ||
                x - shapeIncrease == 2 && y - shapeIncrease == 2 ||
                options.fourthEyeEnabled && x + shapeIncrease == codeMatrix.size - 5 && y + shapeIncrease == codeMatrix.size - 5

    private fun isInsideFrameOrBall(x: Int, y: Int, checkAnchor: Boolean = true): Boolean {
        val shouldSkipVersionEye =
            checkAnchor && options.highlighting.versionEyes !is HighlightingType.None &&
                    anchorCenters.any { x - shapeIncrease in it.first - 2 until it.first + 3 && y - shapeIncrease in it.second - 2 until it.second + 3 }

        return shouldSkipVersionEye || x - shapeIncrease in -1..7 && y - shapeIncrease in -1..7 ||
                x - shapeIncrease in -1..7 && y + shapeIncrease in codeMatrix.size - 8 until codeMatrix.size + 1 ||
                x + shapeIncrease in codeMatrix.size - 8 until codeMatrix.size + 1 && y - shapeIncrease in -1..7 ||
                options.fourthEyeEnabled && x + shapeIncrease in codeMatrix.size - 8 until codeMatrix.size + 1 && y + shapeIncrease in codeMatrix.size - 8 until codeMatrix.size + 1
    }

    private fun createLogo(logoSize: Float): Bitmap? =
        if (options.logo.drawable != null) {
            options.logo.scale.scale(
                options.logo.drawable, logoSize.toInt(), logoSize.toInt()
            ).let { if (it.isMutable) it else it.copy(it.config, true) }.applyCanvas {
                val clip = Path().apply {
                    addRect(0f, 0f, logoSize, logoSize, Path.Direction.CW)
                } - (options.logo.shape.createPath(logoSize, Neighbors.Empty))

                withClip(clip) {
                    drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply {
                        color = Color.TRANSPARENT
                        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
                        isAntiAlias = true
                    })
                }
            }
        } else null

    private fun createBackground(): Bitmap? =
        if (options.background.drawable != null) {

            options.background.scale.scale(
                options.background.drawable, bounds.width(), bounds.height()
            )
        } else null

    private fun createPatterns(
        pixelSize: Float,
        darkPixelPath: Path,
        lightPixelPath: Path,
    ) {

        val rotatedFramePath by rotatedFramePath(pixelSize)
        val rotatedBallPath by rotatedBallPath(pixelSize)

        for (y in 0 until codeMatrix.size) {
            for (x in 0 until codeMatrix.size) {

                val neighbors = codeMatrix.neighbors(x, y)

                val darkPath = darkPathFactory(pixelSize)

                val lightPath = lightPathFactory(pixelSize)

                when {
                    !shouldSeparateFrames && isFrameStart(x, y) -> {
                        darkPixelPath
                            .addPath(rotatedFramePath, x * pixelSize, y * pixelSize)
                    }

                    !shouldSeparateBalls && isBallStart(x, y) -> {
                        darkPixelPath
                            .addPath(rotatedBallPath, x * pixelSize, y * pixelSize)
                    }

                    isInsideFrameOrBall(x, y) -> Unit
                    isInsideVersionEye(x, y)
                            && options.highlighting.versionEyes !is HighlightingType.None  -> Unit
                    isOnTimingLine(x, y)
                            && options.highlighting.timingLines !is HighlightingType.None -> Unit

                    !shouldSeparateDarkPixels
                            && codeMatrix[x,y] == QrCodeMatrix.PixelType.DarkPixel -> {
                        darkPixelPath.addPath(darkPath.next(neighbors), x * pixelSize, y * pixelSize)
                    }

                    !shouldSeparateLightPixels
                            && codeMatrix[x,y] == QrCodeMatrix.PixelType.LightPixel  -> {
                        lightPixelPath.addPath(lightPath.next(neighbors) ,x * pixelSize, y * pixelSize)
                    }
                }
            }
        }
    }

    private fun resize(width: Int, height: Int) {

        val darkPixelPath = Path()
        val lightPixelPath = Path()

        val size = minOf(width, height) * (1 - options.padding.coerceIn(0f, .5f))

        if (size <= Float.MIN_VALUE) {
            return
        }

        val pixelSize = size / codeMatrix.size

        colorFilter = mColorFilter
        alpha = mAlpha

        val logoSize = size * options.logo.size

        val logoBgSize = (logoSize * (1 + options.logo.padding.value)).roundToInt()
        if (options.logo.padding is QrVectorLogoPadding.Natural) {
            applyNaturalLogo(logoBgSize, size, pixelSize)
        }

        val logoBackgroundPath = options.logo.shape
            .createPath(logoBgSize.toFloat(), Neighbors.Empty)


        val logoPaint = when {
            options.logo.padding is QrVectorLogoPadding.Empty -> null
            options.logo.backgroundColor is QrVectorColor.Unspecified -> options.background.color
            else -> options.logo.backgroundColor
        }?.run {
            Paint().apply {
                paint(
                    width = logoBgSize.toFloat(),
                    height = logoBgSize.toFloat(),
                    neighbors = Neighbors.Empty
                )
            }
        }

        createPatterns(
            pixelSize,
            darkPixelPath,
            lightPixelPath
        )

        val logo = createLogo(logoSize)

        val background = createBackground()

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setHasAlpha(true)
            applyCanvas {
                drawToCanvas(
                    canvas = this,
                    size = size,
                    pixelSize = pixelSize,
                    darkPixelPath = darkPixelPath,
                    lightPixelPath = lightPixelPath,
                    logoBgSize = logoBgSize.toFloat(),
                    logoBgPath = logoBackgroundPath,
                    logoBgPaint = logoPaint,
                    logo = logo,
                    background = background
                )
            }
        }
    }
}

private fun QrErrorCorrectionLevel.fit(
    logo: QrVectorLogo, shape : QrShape
) : QrErrorCorrectionLevel  {

    val size = logo.size * (1 + logo.padding.value) * shape.shapeSizeIncrease
    val hasLogo = size > Float.MIN_VALUE && logo.drawable == null ||
            logo.padding != QrVectorLogoPadding.Empty

    return fit(hasLogo, size)
}



private fun <T> List<T>.pairCombinations() : List<Pair<T,T>> {
    return buildList(size * size) {
        this@pairCombinations.forEach { a ->
            this@pairCombinations.forEach { b ->
                add(a to b)
            }
        }
    }.toSet().toList()
}

private val DefaultVersionFrame = QrVectorFrameShape.Rect(5) + QrVectorPixelShape.Rect(1/5f)
private val DefaultHighlightedElementColor = QrVectorColor.Solid(Color.BLACK)
private val DefaultHighlightingColor =  QrVectorColor.Solid(Color.WHITE)
private val DefaultTimingLinePixel get() = QrVectorPixelShape.Default

private class Recreating<T>(
    private val factory : () -> T
) : Lazy<T> {
    override val value: T
        get() = factory()

    override fun isInitialized(): Boolean = true
}

fun interface PixelPathFactory {
    fun next(neighbors: Neighbors) : Path
}

fun interface PixelPaintFactory {
    fun next(neighbors: Neighbors) : Paint
}