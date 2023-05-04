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
    charset: Charset?=null
) : Drawable = QrCodeDrawableImpl(data, options, charset)

private class QrCodeDrawableImpl(
    data: QrData,
    private val options: QrVectorOptions,
    charset: Charset?=null
) : Drawable() {

    private var anchorCenters: List<Pair<Int, Int>>

    private val initialMatrix: QrCodeMatrix

    private val shapeIncrease : Int

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

        val (max, min) = code.version.alignmentPatternCenters.let { (it.maxOrNull() ?:0) to (it.minOrNull() ?: 0) }

        anchorCenters = code.version.alignmentPatternCenters
            .toList().pairCombinations().filterNot {
                it.first == min && it.second == min ||
                        it.first == max && it.second == min ||
                        it.first == min && it.second == max ||
                        options.fourthEyeEnabled && it.first == max && it.second == max
            }

        if (options.highlighting.versionEyes is HighlightingType.Styled){
            initialMatrix.apply {
                anchorCenters.forEach {
                    for(x in it.first - 2 until it.first+3){
                        for (y in it.second -2 until  it.second + 3){
                            this[x, y] = QrCodeMatrix.PixelType.VersionEye
                        }
                    }
                }
            }
        }
//        if (options.anchorHighlighting.timingLines !is HighlightingType.None){
//            initialMatrix.apply {
//                anchorCenters.forEach {
//                    for(x in it.first - 2 until it.first+3){
//                        for (y in it.second -2 until  it.second + 3){
//                            this[x, y] = if (this[x,y] == QrCodeMatrix.PixelType.DarkPixel)
//                                QrCodeMatrix.PixelType.TimingLineDark else QrCodeMatrix.PixelType.TimingLineLight
//                        }
//                    }
//                }
//            }
//        }
    }

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
        resize(right - left, bottom - top)
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

    private fun Canvas.drawBalls(pixelSize: Float, ballPath: Path, ballPaint: Paint) {

        var ballNumber = -1

        balls.forEach {

            val ballPath = if (options.shapes.centralSymmetry) {
                ballNumber += 1
                Path(ballPath).apply {
                    val angle = when (ballNumber) {
                        0 -> 0f
                        1 -> -90f
                        2 -> 90f
                        else -> 180f
                    }
                    transform(rotationMatrix(angle, pixelSize * 3 / 2, pixelSize * 3 / 2))
                }
            } else {
                ballPath
            }

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

        anchorCenters.forEach {
            withTranslation(
                (shapeIncrease + it.first - 2) * pixelSize,
                (shapeIncrease + it.second - 2) * pixelSize
            ) {

                drawPath(highlightShape, highlightPaint)

                if (options.colors.frame !is QrVectorColor.Unspecified) {
                    drawPath(
                        frameShape,
                        options.colors.frame.createPaint(pixelSize * 5, pixelSize * 5)
                    )
                }

                withTranslation(pixelSize * 2, pixelSize * 2) {
                    if (options.colors.frame !is QrVectorColor.Unspecified) {
                        drawPath(
                            ballShape,
                            options.colors.ball.createPaint(pixelSize, pixelSize)
                        )
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

    private fun Canvas.drawFrames(pixelSize: Float, framePath: Path, framePaint: Paint) {
        var frameNumber = -1

        frames.forEach {
            withTranslation(
                it.first * pixelSize,
                it.second * pixelSize
            ) {
                val framePath = if (options.shapes.centralSymmetry) {
                    frameNumber += 1
                    Path(framePath).apply {
                        val angle = when (frameNumber) {
                            0 -> 0f
                            1 -> -90f
                            2 -> 90f
                            else -> 180f
                        }
                        transform(rotationMatrix(angle, pixelSize * 7 / 2, pixelSize * 7 / 2))
                    }
                } else {
                    framePath
                }
                drawPath(framePath, framePaint)
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
        framePath: Path,
        ballPath: Path,
        background: Bitmap?,
        logoBgSize: Float,
        logoBgPath: Path,
        logoBgPaint: Paint?,
        logo: Bitmap?
    ) {
        val darkPixelPaint = options.colors.dark.createPaint(
            codeMatrix.size * pixelSize,
            codeMatrix.size * pixelSize,
        ).apply { isAntiAlias = true }

        val lightPixelPaint = options.colors.light.createPaint(
            codeMatrix.size * pixelSize,
            codeMatrix.size * pixelSize
        ).apply { isAntiAlias = true }

        val ballPaint = options.colors.ball.createPaint(
            pixelSize * 3f,
            pixelSize * 3f,
        ).apply { isAntiAlias = true }

        val framePaint = options.colors.frame.createPaint(
            pixelSize * 7f,
            pixelSize * 7f,
        ).apply { isAntiAlias = true }


        val (w, h) = bounds.width() to bounds.height()

        val (offsetX, offsetY) = with(options.offset) { listOf(x, y) }
            .map { it.coerceIn(-1f, 1f) + 1 }

        val density = canvas.density
        canvas.density = Bitmap.DENSITY_NONE

        canvas.drawBg(background)

        canvas.withTranslation(
            (w - size) / 2f * offsetX,
            (h - size) / 2f * offsetY
        ) {

            canvas.highlightCornerEyes(pixelSize)
            canvas.highlightVersionEyes(pixelSize)

            drawPath(darkPixelPath, darkPixelPaint)
            drawPath(lightPixelPath, lightPixelPaint)

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


            if (options.colors.frame !is QrVectorColor.Unspecified) {
                drawFrames(pixelSize, framePath, framePaint)
            }

            if (options.colors.ball !is QrVectorColor.Unspecified) {
                drawBalls(pixelSize, ballPath, ballPaint)
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
        framePath: Path,
        ballPath: Path,
        darkPixelPath: Path,
        lightPixelPath: Path,
        darkTimingPath : Path,
        lightTimingPath : Path
    ) {

        var frameNumber = -1
        var ballNumber = -1
        for (x in 0 until codeMatrix.size) {
            for (y in 0 until codeMatrix.size) {

                val neighbors = codeMatrix.neighbors(x, y)

                val darkPath = options.shapes.darkPixel
                    .createPath(pixelSize, neighbors)
                val lightPath = options.shapes.lightPixel
                    .createPath(pixelSize, neighbors)

                val timingLinePath = when (options.highlighting.timingLines) {
                    HighlightingType.None -> Path()

                    HighlightingType.Default -> QrVectorPixelShape.Default
                        .createPath(pixelSize, neighbors)

                    is HighlightingType.Styled -> options.highlighting.timingLines.shape
                        ?.createPath(pixelSize, neighbors)
                        ?: if (codeMatrix[x, y] == QrCodeMatrix.PixelType.DarkPixel)
                            options.shapes.darkPixel
                                .createPath(
                                    pixelSize,
                                    if (options.colors.dark.isTransparent) Neighbors.Empty else neighbors
                                )
                        else options.shapes.lightPixel
                            .createPath(
                                pixelSize,
                                if (options.colors.light.isTransparent) Neighbors.Empty else neighbors
                            )
                }

                when {
                    options.colors.frame is QrVectorColor.Unspecified && isFrameStart(x, y) -> {
                        val mFramePath = if (options.shapes.centralSymmetry) {
                            frameNumber = (frameNumber + 1)
                            Path(framePath).apply {
                                val angle = when (frameNumber) {
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
                        } else {
                            framePath
                        }
                        darkPixelPath
                            .addPath(mFramePath, x * pixelSize, y * pixelSize)
                    }

                    options.colors.ball is QrVectorColor.Unspecified && isBallStart(x, y) -> {
                        val mBallPath = if (options.shapes.centralSymmetry) {
                            ballNumber += 1
                            Path(ballPath).apply {
                                val angle = when (ballNumber) {
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
                        } else {
                            ballPath
                        }
                        darkPixelPath
                            .addPath(mBallPath, x * pixelSize, y * pixelSize)
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

                    else -> when (codeMatrix[x, y]) {
                        QrCodeMatrix.PixelType.DarkPixel -> darkPixelPath
                            .addPath(darkPath, x * pixelSize, y * pixelSize)

                        QrCodeMatrix.PixelType.LightPixel -> lightPixelPath
                            .addPath(lightPath, x * pixelSize, y * pixelSize)

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

        val ballPath = ballShape.createPath(pixelSize * 3f, Neighbors.Empty)
        val framePath = frameShape.createPath(pixelSize * 7f, Neighbors.Empty)

        val logoSize = size * options.logo.size

        val logoBgSize = (logoSize * (1 + options.logo.padding.value)).roundToInt()
        if (options.logo.padding is QrVectorLogoPadding.Natural) {
            applyNaturalLogo(logoBgSize, size, pixelSize)
        }

        val logoBackgroundPath =
            options.logo.shape.createPath(logoBgSize.toFloat(), Neighbors.Empty)

        val logoPaint = when {
            options.logo.padding is QrVectorLogoPadding.Empty -> null
            options.logo.backgroundColor is QrVectorColor.Unspecified -> options.background.color
            else -> options.logo.backgroundColor
        }?.createPaint(logoBgSize.toFloat(), logoBgSize.toFloat())

        createMainElements(pixelSize, framePath, ballPath, darkPixelPath, lightPixelPath, darkTimingPath, lightTimingPath)

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
                    framePath = framePath,
                    ballPath = ballPath,
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

    override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {
        val width = size/5f
        addRect(0f,0f,size,width,Path.Direction.CW)
        addRect(0f,0f,width,size,Path.Direction.CW)
        addRect(size-width,0f,size,size,Path.Direction.CW)
        addRect(0f,size-width,size,size,Path.Direction.CW)
    }
}