package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
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

    private val qrOptions by lazy {
        QrOptions.Builder(1024)
            .setPadding(.3f)
            .setBackground(
                QrBackgroundImage(
                    drawable = ContextCompat
                        .getDrawable(this, R.drawable.frame)!!,
                )
            )
            .setLogo(
                QrLogo(
                    drawable = ContextCompat
                        .getDrawable(this, R.drawable.tg)!!,
                    size = .2f,
                    padding = .2f,
                    shape = QrLogoShape
                        .Circle
                )
            )
            .setColors(
                QrColors(
                    dark = QrColor
                        .Solid(Color.parseColor("#345288")),
                    bitmapBackground = QrColor.Solid(Color.WHITE),
                    codeBackground = QrColor
                        .Solid(Color.parseColor("#ddffffff")),
                )
            )
            .setElementsShapes(
                QrElementsShapes(
                    darkPixel = QrPixelShape
                        .RoundCorners(),
                    ball = QrBallShape
                        .RoundCorners(.25f),
                    frame = QrFrameShape
                        .RoundCorners(.25f),
                    background = QrBackgroundShape
                        .RoundCorners(.05f)
                )
            )
            .build()
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

        var oldBmp : Bitmap? = null
        binding.create.setOnClickListener {
            lifecycleScope.launchWhenStarted {
                val bmp = qrGenerator.generateQrCodeSuspend(
                    QrData.Url(binding.editText.text.toString()), qrOptions
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
