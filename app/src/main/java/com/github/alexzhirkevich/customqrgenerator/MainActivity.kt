package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.github.alexzhirkevich.customqrgenerator.example.R
import com.github.alexzhirkevich.customqrgenerator.example.databinding.ActivityMainBinding
import com.github.alexzhirkevich.customqrgenerator.style.*
import java.io.File
import java.io.FileOutputStream


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

    private val qrGenerator: QrCodeGenerator = QrGenerator()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.create.setOnClickListener {
            lifecycleScope.launchWhenStarted {
                val bmp = qrGenerator.generateQrCodeSuspend(
                    QrData.Url(binding.editText.text.toString()), qrOptions
                )
                with(binding.qrCode) {
                    setImageBitmap(bmp)
                    setBackgroundResource(0)
                }
            }
        }
    }
}
