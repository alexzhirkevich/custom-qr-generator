package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.github.alexzhirkevich.customqrgenerator.example.R
import com.github.alexzhirkevich.customqrgenerator.example.databinding.ActivityMainBinding
import com.github.alexzhirkevich.customqrgenerator.style.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val qrOptions by lazy {
         QrOptions.Builder(1024)
            .setPadding(6)
            .setBackground(
                QrBackground(
                    drawable = ContextCompat
                        .getDrawable(this, R.drawable.frame)!!,
                    alpha = 1f
            ))
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
            .setLightColor(Color.WHITE)
            .setDarkColor(Color.parseColor("#345288"))
            .setStyle(
                QrStyle(
                    pixel = QrPixelStyle.RoundCorners,
                    ball = QrBallStyle.RoundCorners(.3f),
                    frame = QrFrameStyle.RoundCorners(.3f),
                    shape = QrShape.RoundCorners(.1f)
                )
            )
            .build()
    }

    private val qrGenerator : QrCodeCreator = QRGenerator()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.create.setOnClickListener {
            lifecycleScope.launchWhenStarted {
                withContext(Dispatchers.Default) {
                    //better to create qr-codes in background
                    qrGenerator.createQrCode(binding.editText.text.toString(), qrOptions).also {
                        withContext(Dispatchers.Main) {
                            with (binding.qrCode){
                                setImageBitmap(it)
                                setBackgroundResource(0)
                            }
                        }
                    }
                }
            }
        }
    }

}
