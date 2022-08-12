package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.alexzhirkevich.customqrgenerator.example.R
import com.github.alexzhirkevich.customqrgenerator.example.databinding.ActivityMainBinding
import com.github.alexzhirkevich.customqrgenerator.style.*

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val options by lazy {
        createQrOptions(1024, .3f) {
            backgroundImage = QrBackgroundImage(
                drawable = ContextCompat
                    .getDrawable(this@MainActivity, R.drawable.frame)!!
            )
            logo = QrLogo(
                drawable = ContextCompat
                    .getDrawable(this@MainActivity, R.drawable.tg)!!,
                size = .25f,
                padding = QrLogoPadding.Natural(.15f),
                shape = QrLogoShape.Circle
            )
            colors = QrColors(
                dark = QrColor.Solid(Color(0xff345288)),
                highlighting = QrColor.Solid(Color(0xddffffff)),
            )
            elementsShapes = QrElementsShapes(
                darkPixel = QrPixelShape.RoundCorners(),
                lightPixel = QrPixelShape.RoundCorners(),
                ball = QrBallShape.Default,
                frame = QrFrameShape.RoundCorners(.25f),
                hightlighting = QrHighlightingShape.RoundCorners(.05f)
            )
        }
    }

    // Use wisely. More threads doesn't mean more performance.
    // It depends on device and QrOptions.size
    private val threadPolicy = when(Runtime.getRuntime().availableProcessors()){
        in 1..3 -> QrGenerator.ThreadPolicy.SingleThread
        in 4..6 -> QrGenerator.ThreadPolicy.DoubleThread
        else -> QrGenerator.ThreadPolicy.QuadThread
    }

    private val qrGenerator: QrCodeGenerator = QrGenerator(threadPolicy)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        var oldBmp : Bitmap?=null
        binding.create.setOnClickListener {
            lifecycleScope.launchWhenStarted {

                val bmp = qrGenerator.generateQrCodeSuspend(
                    QrData.Url(binding.editText.text.toString()), options
                )

                with(binding.qrCode) {
                    setImageBitmap(bmp)
                    setBackgroundResource(0)
                }
                oldBmp?.recycle()
                oldBmp = bmp
            }
        }
    }
}
