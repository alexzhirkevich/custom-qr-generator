package com.github.alexzhirkevich.customqrgenerator.vector

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.*
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.QrErrorCorrectionLevel
import com.github.alexzhirkevich.customqrgenerator.encoder.QrCodeMatrix
import com.github.alexzhirkevich.customqrgenerator.encoder.neighbors
import com.github.alexzhirkevich.customqrgenerator.encoder.toQrMatrix
import com.github.alexzhirkevich.customqrgenerator.fit
import com.github.alexzhirkevich.customqrgenerator.style.DrawableSource
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoPadding
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.encoder.Encoder
import kotlinx.coroutines.runBlocking
import java.nio.charset.Charset
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Vector QR code image
 * */
fun QrCodeDrawable(
    context: Context,
    data: QrData,
    options: QrVectorOptions,
    charset: Charset?=null
) : Drawable = QrCodeDrawableImpl(context, data, options, charset)


internal class QrCodeDrawableImpl(
    context: Context,
    data: QrData,
    private val options: QrVectorOptions,
    charset: Charset?=null
) : Drawable() {

    private val initialMatrix  = Encoder.encode(
        /* content = */ data.encode(),
        /* ecLevel = */ with(options.errorCorrectionLevel) {
            if (this == QrErrorCorrectionLevel.Auto)
                fit(options.logo.size, options.logo.padding.value).lvl
            else lvl
        },
        /* hints = */ charset?.let {
            mapOf(EncodeHintType.CHARACTER_SET to it)
        })
        .matrix.toQrMatrix()

    private val codeMatrix = options.codeShape.apply(initialMatrix)

    private val shapeIncrease = ((initialMatrix.size * options.codeShape.shapeSizeIncrease).roundToInt() - initialMatrix.size)/2

    private var mColorFilter : ColorFilter?=null
    private var mAlpha  = 255

    private val logoDrawable = runBlocking {
        options.logo.drawable.get(context)
    }

    override fun setAlpha(alpha: Int) {
        mAlpha = alpha
        listOf(darkPixelPaint, lightPixelPaint,ballPaint,framePaint)
            .onEach {
                it.alpha = alpha
            }
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mColorFilter = colorFilter
        listOf(darkPixelPaint, lightPixelPaint,ballPaint,framePaint)
            .onEach {
                it.colorFilter = colorFilter
            }

    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    private val ballShape = options.shapes.ball.takeIf {
        it !is QrVectorBallShape.AsDarkPixels
    } ?: QrVectorBallShape.AsPixelShape(options.shapes.darkPixel)

    private val frameShape = options.shapes.frame.takeIf {
        it !is QrVectorFrameShape.AsDarkPixels
    } ?: QrVectorFrameShape.AsPixelShape(options.shapes.darkPixel)

    private var size : Float = 0f

    private var pixelSize : Float = 0f
    private var ballPath : Path = Path()
    private var framePath : Path = Path()

    private var logo : Bitmap?= null
    private var logoBg : Bitmap?= null

    private var darkPixelPath : Path = Path()
    private var lightPixelPath : Path = Path()

    private var darkPixelPaint : Paint = Paint()
    private var lightPixelPaint: Paint = Paint()
    private var framePaint: Paint = Paint()
    private var ballPaint: Paint = Paint()

    override fun setBounds(bounds: Rect) {
        super.setBounds(bounds)
        resize()
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        resize()
    }

    override fun draw(canvas: Canvas) {

        if (pixelSize < Float.MIN_VALUE)
            return

        val (w,h) = with(bounds){ width() to height() }
        val (offsetX, offsetY) = with(options.offset){ listOf(x,y) }
            .map { it.coerceIn(-1f, 1f) + 1 }

        canvas.withTranslation(
            (w - size)/2f * offsetX,
            (h - size)/2f * offsetY
        ) {

            drawPath(darkPixelPath, darkPixelPaint)
            drawPath(lightPixelPath, lightPixelPaint)

            if (options.colors.ball !is QrVectorColor.Unspecified) {
                listOf(
                    2 to 2,
                    2 to codeMatrix.size - 5,
                    codeMatrix.size - 5 to 2
                ).forEach {
                    withTranslation(
                        it.first * pixelSize,
                        it.second * pixelSize
                    ) {
                        drawPath(ballPath, ballPaint)
                    }
                }
            }

            if (options.colors.frame !is QrVectorColor.Unspecified) {
                listOf(
                    0 to 0,
                    0 to codeMatrix.size - 7,
                    codeMatrix.size - 7 to 0
                ).forEach {
                    withTranslation(
                        it.first * pixelSize,
                        it.second * pixelSize
                    ) {
                        drawPath(framePath, framePaint)
                    }
                }
            }
            val nLogo = logo
            if (nLogo != null){
                val (x,y) = (size - nLogo.width) /2f to (size - nLogo.height) /2f
                drawBitmap(nLogo, x,y, null)
            }

            val nLogoBg = logoBg
            if (nLogoBg!= null){
                val (x,y) = (size - nLogoBg.width) /2f to (size - nLogoBg.height) /2f
                drawBitmap(nLogoBg, x,y, null)
            }
        }


    }

    private fun resize(){
        size = minOf(bounds.width(), bounds.height()) *
                (1 - options.padding.coerceIn(0f,.5f))
        pixelSize = size / codeMatrix.size

        ballPath = ballShape.createPath(pixelSize * 3f, Neighbors.Empty)
        framePath = frameShape.createPath(pixelSize * 7f, Neighbors.Empty)

        val singleDarkPixelPath = with(options.shapes.darkPixel) {
            if (isDependOnNeighbors)
                null else createPath(
                pixelSize,
                Neighbors.Empty
            )
        }
        val singleLightPixelPath = with(options.shapes.lightPixel){
            if (isDependOnNeighbors)
                null else createPath(pixelSize, Neighbors.Empty)
        }
        darkPixelPaint = options.colors.dark.createPaint(
                codeMatrix.size * pixelSize,
                codeMatrix.size * pixelSize,
        ).apply { isAntiAlias = true }
        lightPixelPaint = options.colors.light.createPaint(
                codeMatrix.size * pixelSize,
                codeMatrix.size * pixelSize
        ).apply { isAntiAlias = true }
        ballPaint = options.colors.ball.createPaint(
                pixelSize * 3f,
                pixelSize * 3f,
        ).apply { isAntiAlias = true }
        framePaint = options.colors.frame.createPaint(
                pixelSize * 7f,
                pixelSize * 7f,
        ).apply { isAntiAlias = true }

        colorFilter = mColorFilter
        alpha = mAlpha

        darkPixelPath = Path()
        lightPixelPath = Path()

        val logoSize = size * options.logo.size
        val logoBgSize = (logoSize * (1 + options.logo.padding.value)).roundToInt()

        val bgPath1 = options.logo.shape.createPath(
            size = logoBgSize.toFloat(),
            neighbors = Neighbors.Empty
        ).apply {
            transform(translationMatrix(
                (size - logoBgSize) / 2f,
                (size - logoBgSize) / 2f,
            ))
        }
        repeat(2) {
            for (x in 0 until codeMatrix.size) {
                for (y in 0 until codeMatrix.size) {

                    val neighbors = codeMatrix.neighbors(x, y)

                    val darkPath = singleDarkPixelPath ?: options.shapes.darkPixel
                        .createPath(pixelSize, neighbors)
                    val lightPath = singleLightPixelPath ?: options.shapes.lightPixel
                        .createPath(pixelSize, neighbors)


                    when {
                        options.colors.frame is QrVectorColor.Unspecified &&
                                x - shapeIncrease == 0 && y - shapeIncrease == 0 ||
                                x - shapeIncrease == 0 && y + shapeIncrease == codeMatrix.size - 7 ||
                                x + shapeIncrease == codeMatrix.size - 7 && y - shapeIncrease == 0 -> darkPixelPath.addPath(
                            framePath,
                            x * pixelSize,
                            y * pixelSize
                        )

                        options.colors.ball is QrVectorColor.Unspecified &&
                                x - shapeIncrease == 2 && y + shapeIncrease == codeMatrix.size - 5 ||
                                x + shapeIncrease == codeMatrix.size - 5 && y - shapeIncrease == 2 ||
                                x - shapeIncrease == 2 && y - shapeIncrease == 2 -> {
                            darkPixelPath.addPath(
                                ballPath,
                                x.toFloat() * pixelSize,
                                y.toFloat() * pixelSize
                            )
                        }

                        x - shapeIncrease in -1..7 && y - shapeIncrease in -1..7 ||
                                x - shapeIncrease in -1..7 && y + shapeIncrease in codeMatrix.size - 8 until codeMatrix.size + 1 ||
                                x + shapeIncrease in codeMatrix.size - 8 until codeMatrix.size + 1 && y - shapeIncrease in -1..7 -> Unit

                        options.logo.padding is QrVectorLogoPadding.Natural &&
                                (codeMatrix[x, y] == QrCodeMatrix.PixelType.DarkPixel &&
                                        bgPath1.and(
                                            Path(darkPath).apply {
                                                transform(
                                                    translationMatrix(
                                                        x * pixelSize,
                                                        y * pixelSize
                                                    )
                                                )
                                            }).isEmpty.not()) ||
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
                                        ) -> {
                            codeMatrix[x, y] = QrCodeMatrix.PixelType.Logo
                        }

                        else -> when (codeMatrix[x, y]) {
                            QrCodeMatrix.PixelType.DarkPixel ->
                                darkPixelPath.addPath(
                                    darkPath,
                                    x.toFloat() * pixelSize,
                                    y.toFloat() * pixelSize
                                )
                            QrCodeMatrix.PixelType.LightPixel ->
                                lightPixelPath.addPath(
                                    lightPath,
                                    x.toFloat() * pixelSize,
                                    y.toFloat() * pixelSize
                                )
                        }
                    }
                }
            }
        }

        if (options.logo.padding is QrVectorLogoPadding.Accurate) {

            val bgPath = options.logo.shape.createPath(
                size = logoBgSize.toFloat(),
                neighbors = Neighbors.Empty
            )

            logoBg =if (options.logo.backgroundColor != QrVectorColor.Unspecified) {

                 Bitmap.createBitmap(
                    logoBgSize, logoBgSize, Bitmap.Config.ARGB_8888
                ).applyCanvas {
                    drawPath(
                        bgPath,
                        options.logo.backgroundColor.createPaint(
                            logoBgSize.toFloat(), logoBgSize.toFloat()
                        )
                    )
                }
            } else {
                bgPath.transform(
                    translationMatrix(
                        (size - logoBgSize) / 2f,
                        (size - logoBgSize) / 2f,
                    )
                )
                darkPixelPath -= bgPath
                lightPixelPath -= bgPath
                null
            }
        }

        logo = if (options.logo.drawable != DrawableSource.Empty) {
            options.logo.scale.scale(
                logoDrawable, logoSize.roundToInt(), logoSize.roundToInt()
            ).applyCanvas {
                val clip = Path().apply { addRect(0f, 0f, logoSize, logoSize, Path.Direction.CW) }-
                        (options.logo.shape.createPath(logoSize, Neighbors.Empty))

                withClip(clip) {
                    drawRect(0f,0f, width.toFloat(), height.toFloat(), Paint().apply {
                        color = Color.TRANSPARENT
                        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
                        isAntiAlias = true
                    })
                }
            }

//            Bitmap.createBitmap(bmp.width, bmp.height, Bitmap.Config.ARGB_8888).applyCanvas {
//                withClip() {
//                    drawBitmap(bmp, 0f, 0f, null)
//                    this.drawColor(Color.TRANSPARENT)
//                }
//            }
        } else null
    }
}

fun ClosedRange<Int>.containedIn(other: ClosedRange<Int>) =
    (min(endInclusive, other.endInclusive) - max(start, other.start))
        .coerceAtLeast(0)