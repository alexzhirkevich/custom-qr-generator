package com.github.alexzhirkevich.customqrgenerator.vector

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.*
import com.github.alexzhirkevich.customqrgenerator.HighlightingType
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.QrErrorCorrectionLevel
import com.github.alexzhirkevich.customqrgenerator.encoder.QrCodeMatrix
import com.github.alexzhirkevich.customqrgenerator.encoder.neighbors
import com.github.alexzhirkevich.customqrgenerator.encoder.toQrMatrix
import com.github.alexzhirkevich.customqrgenerator.fit
import com.github.alexzhirkevich.customqrgenerator.style.EmptyDrawable
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.style.QrShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.*
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor.Transparent.paint
import com.github.alexzhirkevich.customqrgenerator.vector.style.StarVectorShape.shape
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.encoder.Encoder
import java.nio.charset.Charset
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

/**
 * Vector QR code image
 * */
fun QrCodeDrawable(
    data: QrData,
    options: QrVectorOptions = QrVectorOptions.Builder().build(),
    charset: Charset?=null
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


        shapeIncrease = ((initialMatrix.size * options.codeShape.shapeSizeIncrease)
            .roundToInt() - initialMatrix.size) / 2

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

        if (options.highlighting.versionEyes is HighlightingType.Styled) {
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

    val shouldSeparateFrames
        get() = options.colors.frame.mode == QrPaintMode.Separate ||
                options.colors.dark.mode == QrPaintMode.Separate

    val shouldSeparateBalls
        get() = options.colors.ball.mode == QrPaintMode.Separate ||
                options.colors.dark.mode == QrPaintMode.Separate

    val shouldSeparateDarkPixels
        get() = options.colors.dark.mode == QrPaintMode.Separate

    val shouldSeparateLightPixels
        get() = options.colors.light.mode == QrPaintMode.Separate

    val codeMatrix = options.codeShape.apply(initialMatrix)

    private val balls = mutableListOf(
        2 + shapeIncrease to 2 + shapeIncrease,
        2 + shapeIncrease to codeMatrix.size - 5 - shapeIncrease,
        codeMatrix.size - 5 - shapeIncrease to 2 + shapeIncrease
    ).apply {
        if (options.fourthEyeEnabled)
            this += codeMatrix.size - 5 - shapeIncrease to codeMatrix.size - 5 - shapeIncrease
    }.toList()

    private val frames = mutableListOf(
        shapeIncrease to shapeIncrease,
        shapeIncrease to codeMatrix.size - 7 - shapeIncrease,
        codeMatrix.size - 7 - shapeIncrease to shapeIncrease
    ).apply {
        if (options.fourthEyeEnabled) {
            this += codeMatrix.size - 7 - shapeIncrease to codeMatrix.size - 7 - shapeIncrease
        }
    }.toList()

    private var mColorFilter: ColorFilter? = null
    private var mAlpha = 255

    private val ballShape = options.shapes.ball.takeIf {
        it !is QrVectorBallShape.AsDarkPixels
    } ?: QrVectorBallShape.AsPixelShape(options.shapes.darkPixel)

    private val frameShape = options.shapes.frame.takeIf {
        it !is QrVectorFrameShape.AsDarkPixels
    } ?: QrVectorFrameShape.AsPixelShape(options.shapes.darkPixel)


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
        measureTimeMillis {
            resize(right - left, bottom - top)
        }.also { println("Time elapsed: $it") }
    }

    private fun framePathFactory(pixelSize: Float): Lazy<Path> {
        val path = Path()

        val pathFactory = {
            frameShape.run {
                path.rewind()
                path.shape(pixelSize * 7f, Neighbors.Empty)
            }
        }
        return if (options.colors.frame.mode == QrPaintMode.Combine)
            lazy(pathFactory)
        else Recreating(pathFactory)
    }

    private fun framePaintFactory(pixelSize: Float): Lazy<Paint> {

        val color = options.colors.frame.takeIf { it.isSpecified }
            ?: options.colors.dark

        val paint = Paint()
        val paintFactory = {
            paint.reset()
            color.run {
                paint.paint(pixelSize * 7f, pixelSize *73f)
            }
            paint
        }

        return if (options.colors.frame.mode == QrPaintMode.Combine)
            lazy(paintFactory) else Recreating(paintFactory)
    }

    private fun ballPathFactory(pixelSize: Float): Lazy<Path> {
        val path = Path()
        val pathFactory = {
            path.rewind()
            ballShape.run {
                path.shape(pixelSize * 3f, Neighbors.Empty)
            }
        }
        return if (options.colors.ball.mode == QrPaintMode.Combine)
            lazy(pathFactory)
        else Recreating(pathFactory)
    }

    private fun ballPaintFactory(pixelSize: Float): Lazy<Paint> {

        val color = options.colors.ball.takeIf { it.isSpecified }
            ?: options.colors.dark

        val paint = Paint()
        val paintFactory = {
            paint.reset()
            color.run {
                paint.paint(pixelSize * 3f, pixelSize * 3f,)
            }
            paint
        }

        return if (options.colors.ball.mode == QrPaintMode.Combine)
            lazy(paintFactory) else Recreating(paintFactory)
    }

    private fun darkPaintFactory(pixelSize: Float): Lazy<Paint> {

        val paint = Paint()

        val size = if (shouldSeparateDarkPixels)
            pixelSize else codeMatrix.size * pixelSize

        val paintFactory = {
            paint.reset()
            options.colors.dark.run {
                paint.paint(size, size)
            }
            paint
        }

        return if (shouldSeparateDarkPixels)
            Recreating(paintFactory) else lazy(paintFactory)
    }

    private fun rotatedBallPath(pixelSize: Float): Lazy<Path> {

        var number = -1

        val path = ballPathFactory(pixelSize)

        return if (options.shapes.centralSymmetry) {
            Recreating {
                number++
                Path(path.value).apply {
                    val angle = when (number) {
                        0 -> 0f
                        1 -> -90f
                        2 -> 90f
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
        } else {
            path
        }
    }

    private fun rotatedFramePath(pixelSize: Float): Lazy<Path> {

        var number = -1

        val path = framePathFactory(pixelSize)

        return if (options.shapes.centralSymmetry) {
            Recreating {
                number++
                Path(path.value).apply {
                    val angle = when (number) {
                        0 -> 0f
                        1 -> -90f
                        2 -> 90f
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
        } else {
            path
        }
    }

    private fun lightPathFactory(pixelSize: Float, neighbors: Neighbors) : Lazy<Path> {
        val pathFactory = { options.shapes.darkPixel.createPath(pixelSize, neighbors) }
        return Recreating(pathFactory)
    }

    private fun lightPaintFactory(pixelSize: Float) : Lazy<Paint> {

        val paint = Paint()

        val size = if (shouldSeparateDarkPixels)
            pixelSize else codeMatrix.size * pixelSize

        val paintFactory = {
            paint.reset()

            options.colors.light.run {
                paint.paint(size, size)
            }
            paint
        }

        return if (shouldSeparateLightPixels)
            Recreating(paintFactory) else lazy(paintFactory)
    }

    private fun darkPathFactory(pixelSize: Float, neighbors: Neighbors) : Lazy<Path> {
        val path = Path()
        val pathFactory = {
            path.rewind()
            options.shapes.darkPixel.run {
                path.shape(pixelSize, neighbors)
            }
        }
        return Recreating(pathFactory)
    }


    private fun Canvas.drawBg(background: Bitmap?) {
        if (options.background.color !is QrVectorColor.Unspecified &&
            options.background.color !is QrVectorColor.Transparent
        ) {
            drawPaint(
                options.background.color.createPaint(
                    bounds.width().toFloat(), bounds.height().toFloat()
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
    ) : Paint {

        val styled = highlightingType is HighlightingType.Styled

        val color = (highlightingType as? HighlightingType.Styled)?.color
            ?: options.colors.light.takeIf { it.isTransparent.not() && styled }
            ?: options.background.color.takeIf { it.isTransparent && styled }
            ?: QrVectorColor.Solid(Color.WHITE)

        return color
            .createPaint(size, size)
            .apply { alpha = (options.highlighting.alpha.coerceIn(0f, 1f) * 255).roundToInt() }
    }

    private fun Canvas.highlightVersionEyes(pixelSize: Float) {
        val (frame, ball) = when (options.highlighting.versionEyes) {
            HighlightingType.None -> return
            HighlightingType.Default ->
                DefaultVersionFrame to QrVectorBallShape.Default

            is HighlightingType.Styled ->
                options.shapes.frame to options.shapes.ball
        }

        val frameShape = frame.createPath(pixelSize * 5, Neighbors.Empty)

        val ballShape = ball.createPath(pixelSize, Neighbors.Empty)

        val highlightPaint =
            createHighlightingPaint(options.highlighting.versionEyes, pixelSize * 5)

        val highlightShape = (options.highlighting.versionEyes as? HighlightingType.Styled)
            ?.shape?.createPath(pixelSize * 5, Neighbors.Empty)
            ?: (frameShape + QrVectorBallShape.Default.createPath(pixelSize * 3, Neighbors.Empty)
                .apply { this.transform(translationMatrix(pixelSize ,pixelSize)) })


        val ballPaint = options.colors.ball.createPaint(pixelSize, pixelSize)
        val framePaint = options.colors.frame.createPaint(pixelSize * 5, pixelSize * 5)
        anchorCenters.forEach {
            withTranslation(
                (shapeIncrease + it.first - 2) * pixelSize,
                (shapeIncrease + it.second - 2) * pixelSize
            ) {

                drawPath(highlightShape, highlightPaint)

                if (options.colors.frame !is QrVectorColor.Unspecified) {
                    drawPath(frameShape, framePaint)
                }

                if (options.colors.frame !is QrVectorColor.Unspecified) {
                    withTranslation(pixelSize * 2, pixelSize * 2) {
                        drawPath(ballShape, ballPaint)
                    }
                }
            }
        }
    }

    private fun Canvas.highlightCornerEyes(pixelSize: Float) {

        val shape = when (options.highlighting.cornerEyes) {
            HighlightingType.None -> return
            HighlightingType.Default -> QrVectorBallShape.Default
                .createPath(pixelSize * 9, Neighbors.Empty)

            is HighlightingType.Styled -> options.highlighting.cornerEyes.shape
                ?.createPath(pixelSize * 9, Neighbors.Empty)
                ?: (options.shapes.frame.createPath(pixelSize * 9, Neighbors.Empty) +
                        QrVectorBallShape.Default.createPath(pixelSize * 7, Neighbors.Empty).apply {
                            transform(translationMatrix(pixelSize, pixelSize))
                        })

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
        darkTimingPath: Path,
        lightTimingPath: Path,
        background: Bitmap?,
        logoBgSize: Float,
        logoBgPath: Path,
        logoBgPaint: Paint?,
        logo: Bitmap?
    ) {

        val (w, h) = bounds.width() to bounds.height()

        val (offsetX, offsetY) = with(options.offset) { listOf(x, y) }
            .map { it.coerceIn(-1f, 1f) + 1 }

        val density = canvas.density
        canvas.density = Bitmap.DENSITY_NONE

        val darkPixelPaint by darkPaintFactory(pixelSize)
        val lightPixelPaint by lightPaintFactory(pixelSize)

        canvas.drawBg(background)

        canvas.withTranslation(
            (w - size) / 2f * offsetX,
            (h - size) / 2f * offsetY
        ) {

            canvas.highlightCornerEyes(pixelSize)
            canvas.highlightVersionEyes(pixelSize)

            if (shouldSeparateDarkPixels || shouldSeparateLightPixels) {
                repeat(codeMatrix.size) { i ->
                    repeat(codeMatrix.size) { j ->
                        if (!isInsideFrameOrBall(i, j)) {
                            withTranslation(i * pixelSize, j * pixelSize) {
                                if (shouldSeparateDarkPixels && codeMatrix[i, j] == QrCodeMatrix.PixelType.DarkPixel) {
                                    drawPath(
                                        darkPathFactory(
                                            pixelSize,
                                            codeMatrix.neighbors(i, j)
                                        ).value,
                                        darkPixelPaint
                                    )
                                }
                                if (shouldSeparateLightPixels && codeMatrix[i, j] == QrCodeMatrix.PixelType.LightPixel) {
                                    drawPath(
                                        darkPathFactory(
                                            pixelSize,
                                            codeMatrix.neighbors(i, j)
                                        ).value,
                                        darkPixelPaint
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (!shouldSeparateDarkPixels) {
                drawPath(darkPixelPath, darkPixelPaint)
            }
            if (!shouldSeparateLightPixels) {
                drawPath(lightPixelPath, lightPixelPaint)
            }

            drawPath(darkTimingPath, when (options.highlighting.timingLines){
                HighlightingType.Default -> QrVectorColor.Solid(Color.BLACK)
                    .createPaint(
                    codeMatrix.size * pixelSize,
                    codeMatrix.size * pixelSize,
                )
                HighlightingType.None -> Paint() // path is empty
                is HighlightingType.Styled -> darkPixelPaint
            })

            drawPath(lightTimingPath, when (options.highlighting.timingLines){
                HighlightingType.Default -> QrVectorColor.Solid(Color.WHITE)
                    .createPaint(
                        codeMatrix.size * pixelSize,
                        codeMatrix.size * pixelSize,
                    ).apply { alpha = (options.highlighting.alpha.coerceIn(0f,1f) * 255).roundToInt() }
                HighlightingType.None -> Paint() // path is empty
                is HighlightingType.Styled -> options.highlighting.timingLines.color?.createPaint(
                    codeMatrix.size * pixelSize,
                    codeMatrix.size * pixelSize,
                ) ?: lightPixelPaint
            })


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
                val (x, y) = (size - logo.width) / 2f to (size - logo.height) / 2f
                drawBitmap(logo, x, y, null)
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
        for (x in 0 until codeMatrix.size) {
            for (y in 0 until codeMatrix.size) {

                val neighbors = codeMatrix.neighbors(x, y)
                val darkPath = options.shapes.darkPixel
                    .createPath(pixelSize, neighbors)
                val lightPath = options.shapes.lightPixel
                    .createPath(pixelSize, neighbors)

                if (codeMatrix[x, y] == QrCodeMatrix.PixelType.DarkPixel &&
                    bgPath1.and(Path(darkPath).apply {
                        transform(
                            translationMatrix(
                                x * pixelSize,
                                y * pixelSize
                            )
                        )
                    }).isEmpty.not() ||
                    (codeMatrix[x, y] == QrCodeMatrix.PixelType.LightPixel &&
                            bgPath1.and(
                                Path(lightPath).apply {
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

    private fun isOnTimingLine(x : Int, y : Int) =
        (x - shapeIncrease == 6  || y - shapeIncrease == 6) && !isInsideFrameOrBall(x,y)
    private fun isVersionEyeCenter(x : Int, y : Int) =
        anchorCenters.any { it.first == x - shapeIncrease && it.second == y - shapeIncrease }


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
    private fun isInsideFrameOrBall(x: Int, y: Int, checkAnchor : Boolean = true): Boolean {
        val shouldSkipVersionEye = checkAnchor && options.highlighting.versionEyes !is HighlightingType.None &&
                anchorCenters.any { x - shapeIncrease in it.first - 2 until it.first + 3 && y - shapeIncrease in it.second-2 until it.second + 3 }

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

    private fun createMainElements(
        pixelSize: Float,
        darkPixelPath: Path,
        lightPixelPath: Path,
        darkTimingPath : Path,
        lightTimingPath : Path
    ) {

        val framePath by framePathFactory(pixelSize)
        val ballPath by ballPathFactory(pixelSize)

        var frameNumber = -1
        var ballNumber = -1

        val rotatedFramePath by rotatedFramePath(pixelSize)
        val rotatedBallPath by rotatedBallPath(pixelSize)

        for (x in 0 until codeMatrix.size) {
            for (y in 0 until codeMatrix.size) {

                val neighbors = codeMatrix.neighbors(x, y)

                val darkPath by darkPathFactory(pixelSize, neighbors)

                val lightPath by lightPathFactory(pixelSize, neighbors)

                val timingLinePath = when (options.highlighting.timingLines) {
                    HighlightingType.None -> Path()

                    HighlightingType.Default -> QrVectorPixelShape.Default
                        .createPath(pixelSize, neighbors)

                    is HighlightingType.Styled -> options.highlighting.timingLines.shape
                        ?.createPath(pixelSize, neighbors)
                        ?: if (codeMatrix[x, y] == QrCodeMatrix.PixelType.DarkPixel)
                            darkPathFactory(
                                pixelSize,
                                if (options.colors.dark.isTransparent) Neighbors.Empty else neighbors
                            ).value
                        else lightPathFactory(
                            pixelSize,
                            if (options.colors.light.isTransparent) Neighbors.Empty else neighbors
                        ).value
                }

                when {
                    !shouldSeparateFrames && isFrameStart(x, y) -> {
                        darkPixelPath
                            .addPath(rotatedFramePath, x * pixelSize, y * pixelSize)
                    }

                    !shouldSeparateBalls && isBallStart(x, y) -> {
                        darkPixelPath
                            .addPath(rotatedBallPath, x * pixelSize, y * pixelSize)
                    }

                    options.highlighting.versionEyes !is HighlightingType.None &&
                            (options.colors.frame is QrVectorColor.Unspecified ||
                                    options.colors.ball is QrVectorColor.Unspecified) &&
                            isVersionEyeCenter(x, y) -> {
                        if (options.colors.frame is QrVectorColor.Unspecified) {

                            val shape =
                                (if (options.highlighting.versionEyes is HighlightingType.Styled)
                                    options.shapes.frame else DefaultVersionFrame)
                                    .createPath(pixelSize * 5, Neighbors.Empty)

                            darkPixelPath.addPath(
                                shape, (x - 2) * pixelSize, (y - 2) * pixelSize
                            )
                        }
                        if (options.colors.ball is QrVectorColor.Unspecified) {

                            val shape =
                                (if (options.highlighting.versionEyes is HighlightingType.Styled)
                                    options.shapes.ball else QrVectorBallShape.Default)
                                    .createPath(pixelSize, Neighbors.Empty)

                            darkPixelPath.addPath(
                                shape, x * pixelSize, y * pixelSize
                            )
                        }
                    }

                    isInsideFrameOrBall(x, y) -> Unit

                    options.highlighting.timingLines !is HighlightingType.None &&
                            isOnTimingLine(x, y) -> when (codeMatrix[x, y]) {
                        QrCodeMatrix.PixelType.DarkPixel -> darkTimingPath
                            .addPath(timingLinePath, x * pixelSize, y * pixelSize)

                        QrCodeMatrix.PixelType.LightPixel -> lightTimingPath
                            .addPath(timingLinePath, x * pixelSize, y * pixelSize)

                        else -> {}
                    }

                    else -> when {
                        codeMatrix[x, y] == QrCodeMatrix.PixelType.DarkPixel && !shouldSeparateDarkPixels ->
                            darkPixelPath.addPath(darkPath, x * pixelSize, y * pixelSize)

                        codeMatrix[x, y] == QrCodeMatrix.PixelType.LightPixel && !shouldSeparateLightPixels ->
                            lightPixelPath.addPath(lightPath, x * pixelSize, y * pixelSize)

                        else -> {}
                    }
                }
            }
        }
    }

    private fun resize(width: Int, height: Int) {

        val darkPixelPath = Path()
        val lightPixelPath = Path()
        val darkTimingPath = Path()
        val lightTimingPath = Path()

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
            Paint().apply { paint(logoBgSize.toFloat(), logoBgSize.toFloat()) }
        }

        createMainElements(pixelSize, darkPixelPath, lightPixelPath, darkTimingPath, lightTimingPath)

        val logo = createLogo(logoSize)

        val background = createBackground()

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap?.apply {
            setHasAlpha(true)
            applyCanvas {
                drawToCanvas(
                    canvas = this,
                    size = size,
                    pixelSize = pixelSize,
                    darkPixelPath = darkPixelPath,
                    lightPixelPath = lightPixelPath,
                    darkTimingPath = darkTimingPath,
                    lightTimingPath = lightTimingPath,
                    background = background,
                    logoBgSize = logoBgSize.toFloat(),
                    logoBgPath = logoBackgroundPath,
                    logoBgPaint = logoPaint,
                    logo = logo
                )
            }
        }
    }
}

private fun QrErrorCorrectionLevel.fit(
    logo: QrVectorLogo, shape : QrShape
) : QrErrorCorrectionLevel  {
    val size = logo.size * (1 + logo.padding.value) * shape.shapeSizeIncrease
    val hasLogo = size > Float.MIN_VALUE && logo.drawable != EmptyDrawable ||
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

private object DefaultVersionFrame : QrVectorFrameShape {
    override fun Path.shape(size: Float, neighbors: Neighbors) = apply {
        val width = size/5f
        addRect(0f,0f,size,width,Path.Direction.CW)
        addRect(0f,0f,width,size,Path.Direction.CW)
        addRect(size-width,0f,size,size,Path.Direction.CW)
        addRect(0f,size-width,size,size,Path.Direction.CW)
    }

    override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {
        shape(size, neighbors)
    }
}

private class Recreating<T>(
    private val factory : () -> T
) : Lazy<T> {
    override val value: T
        get() = factory()

    override fun isInitialized(): Boolean = true

}