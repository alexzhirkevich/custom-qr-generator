package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.withRotation
import androidx.lifecycle.lifecycleScope
import com.github.alexzhirkevich.customqrgenerator.dsl.draw
import com.github.alexzhirkevich.customqrgenerator.example.R
import com.github.alexzhirkevich.customqrgenerator.example.databinding.ActivityMainBinding
import com.github.alexzhirkevich.customqrgenerator.style.*
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalSerializationApi
class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    //custom color for eyes
    private val eyeColor = QrCanvasColor { canvas ->
        with(canvas) {
            val paint = Paint().apply {
                color = 0xff345288.toColor()
                isAntiAlias = true
            }
            withRotation(135f, width / 2f, height / 2f) {
                drawRect(
                    -width / 2f, -height / 2f,
                    1.5f * width, height / 2f, paint
                )
                paint.color = 0xff31b4d5.toColor()
                drawRect(
                    -width / 2f, height / 2f,
                    1.5f * width, 1.5f * height, paint
                )
            }
        }
    }


    // QR code styling options
    private val options by lazy {
        createQrOptions(1024, 1024, .3f) {

            background {
                drawable = DrawableSource.Resource(R.drawable.frame)
                color = QrColor.Unspecified
            }

            logo {
                drawable = DrawableSource.Resource(R.drawable.tg)
                size = .25f
                padding = QrLogoPadding.Natural(.1f)
                shape = QrLogoShape.Circle
            }

            shapes {
                darkPixel = QrPixelShape.
                    RoundCorners()

                frame = QrFrameShape.RoundCorners(
                    .25f,
                )
                ball = QrBallShape.RoundCorners(
                    .25f,
                )
                highlighting = QrHighlightingShape
                    .RoundCorners(.05f)
            }

            colors {
                dark = QrColor.RadialGradient(
                    startColor = 0xff31b4d5.toColor(),
                    endColor = 0xff345288.toColor(),
                    radius = 2f
                )
                highlighting = QrColor.Solid(0xddffffff.toColor())
                ball = draw(eyeColor::draw)

                frame = draw {
                    withRotation(180f, width/2f,
                        height/2f, eyeColor::draw)
                }
            }
        }
    }


    // QR code generator thread policy.
    // Use wisely. More threads doesn't mean more performance.
    // It depends on device and QrOptions.size
    private val threadPolicy = when (Runtime.getRuntime().availableProcessors()) {
        in 1..3 -> ThreadPolicy.SingleThread
        in 4..6 -> ThreadPolicy.DoubleThread
        else -> ThreadPolicy.QuadThread
    }

    private val qrGenerator: QrCodeGenerator by lazy {
        QrCodeGenerator(this@MainActivity, threadPolicy)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)


        with(binding) {
            editInput.setText(R.string.app_name)
            btnCreate.setOnClickListener {
                lifecycleScope.launchWhenStarted {
                    val bmp = qrGenerator.generateQrCode(
                        QrData.Url(editInput.text.toString()),
                        options
                    )
                    ivQrcode.setImageBitmap(bmp)
                }
            }
        }
    }
}
