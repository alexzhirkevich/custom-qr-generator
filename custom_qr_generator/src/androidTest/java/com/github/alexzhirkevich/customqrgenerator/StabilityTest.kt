package com.github.alexzhirkevich.customqrgenerator

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.alexzhirkevich.customqrgenerator.dsl.drawShape
import com.github.alexzhirkevich.customqrgenerator.style.*

import org.junit.Test
import org.junit.runner.RunWith

import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class StabilityTest {
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val generator = QrCodeGenerator(appContext)
    val data = QrData {
        "qweqwelajjdljasfljasfjlsadjlfadsljkadskfjldfkslkajdsfkjadfsljkasdf" +
                "asfdknfsadkafsd,anfdsnksdfnjkfsdnkjafdnjkafdnskjdsaf" +
                "fadsnfaksdnknfdsankdfjasnkjdfsankjdsfanjkdfsajnk"
    }
    @Test
    fun base() {
        // Context of the app under test.
        generator.generateQrCode(data, createQrOptions(512,512){})
    }

    @Test
    fun nonRectTest() {
        // Context of the app under test.
        generator.generateQrCode(data, createQrOptions(512,1024){})
        generator.generateQrCode(data, createQrOptions(1024,512){})
    }

    @Test
    fun baseShapesTest(){
        generator.generateQrCode(data, createQrOptions(512,512) {
            shapes {
                darkPixel = QrPixelShape.RoundCorners()
                lightPixel = QrPixelShape.Circle()
                ball = QrBallShape.AsPixelShape(darkPixel)
                frame = QrFrameShape.Circle()
                highlighting = QrHighlightingShape.RoundCorners(.05f)
            }
        })
    }

    @Test
    fun baseColorsTest(){
        generator.generateQrCode(data, createQrOptions(512,512) {
            colors {
                dark = QrColor.LinearGradient(
                    android.graphics.Color.RED,
                    android.graphics.Color.BLUE,
                    QrColor.LinearGradient.Orientation.LeftDiagonal
                )
                light = QrColor.Solid(android.graphics.Color.RED)
                ball = QrColor.RadialGradient(
                    android.graphics.Color.RED,
                    android.graphics.Color.BLUE
                )
                frame = QrColor.SquareGradient(
                    android.graphics.Color.RED,
                    android.graphics.Color.BLUE
                )
                highlighting = QrColor.CrossingGradient(
                    android.graphics.Color.RED,
                    android.graphics.Color.BLUE
                )
            }
        })
    }
    @Test
    fun customShapesTest(){
        generator.generateQrCode(data, createQrOptions(512,512){
            shapes {
                darkPixel = drawShape { canvas, drawPaint, _ ->
                    canvas.drawCircle(canvas.width/2f, canvas.height/2f, canvas.width/2f, drawPaint)
                }
                ball = drawShape { canvas, drawPaint, _ ->
                    canvas.drawCircle(canvas.width/2f, canvas.height/2f, canvas.width/2f, drawPaint)
                }
                frame = drawShape { canvas, drawPaint, erasePaint ->
                    canvas.drawCircle(canvas.width/2f, canvas.height/2f, canvas.width/2f, drawPaint)
                }
                highlighting = drawShape { canvas, drawPaint, erasePaint ->
                    canvas.drawCircle(canvas.width/2f, canvas.height/2f, canvas.width/2f, drawPaint)
                }
            }
        })
    }
    @Test
    fun customColorsTest(){
        generator.generateQrCode(data, createQrOptions(512,512){
            colors {
                dark = QrColor { i, j, width, height ->
                    Random.nextInt()
                }
                ball = QrColor { i, j, width, height ->
                    Random.nextInt()
                }
                frame = QrColor { i, j, width, height ->
                    Random.nextInt()
                }
                highlighting  = QrColor { i, j, width, height ->
                    Random.nextInt()
                }
            }
        })
    }

    @Test
    fun logoTest(){
        generator.generateQrCode(data, createQrOptions(512,512){
            logo {
                padding = QrLogoPadding.Natural(.15f)
                drawable = DrawableSource.Resource(android.R.drawable.btn_star_big_on)
                shape = QrLogoShape.Circle
                backgroundColor = QrColor.Solid(0xddffffff.toColor())
                scale = BitmapScale.FitXY
            }
        })
        generator.generateQrCode(data, createQrOptions(512,512){
            logo {
                padding = QrLogoPadding.Accurate(.2f)
                drawable = DrawableSource.Resource(android.R.drawable.btn_star_big_on)
                shape = QrLogoShape.Rhombus
                backgroundColor = QrColor.Solid(0xddffffff.toColor())
                scale = BitmapScale.CenterCrop
            }
        })
    }
    fun bgTest(){
        generator.generateQrCode(data, createQrOptions(512,512){
            background {
                drawable = DrawableSource.Resource(android.R.drawable.btn_star_big_on)
                scale = BitmapScale.FitXY
                alpha = .5f
            }

        })
        generator.generateQrCode(data, createQrOptions(512,512) {
            background {
                drawable = DrawableSource.Resource(android.R.drawable.btn_star_big_on)
                scale = BitmapScale.CenterCrop
                alpha = .23f
            }
        })
    }
    fun offsetTest(){
        generator.generateQrCode(data, createQrOptions(512,512) {
            offset {
                x = .5f
                y = .5f
            }
        })
        generator.generateQrCode(data, createQrOptions(512,512) {
            offset {
                x = 1231f
                y = -321312f
            }
        })
    }
}