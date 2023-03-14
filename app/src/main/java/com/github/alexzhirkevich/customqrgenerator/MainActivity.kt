package com.github.alexzhirkevich.customqrgenerator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.addTextChangedListener
import com.github.alexzhirkevich.customqrgenerator.example.R
import com.github.alexzhirkevich.customqrgenerator.example.databinding.ActivityMainBinding
import com.github.alexzhirkevich.customqrgenerator.style.Color
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.createQrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.*

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val options by lazy {
        createQrVectorOptions {

            padding = .325f

            fourthEyeEnabled = true

            background {
                drawable = ContextCompat
                    .getDrawable(this@MainActivity, R.drawable.frame)
            }

            logo {
                drawable = ContextCompat
                    .getDrawable(this@MainActivity, R.drawable.tg)
                size = .25f
                padding = QrVectorLogoPadding.Natural(.2f)
                shape = QrVectorLogoShape
                    .Circle
            }
            colors {
                dark = QrVectorColor
                    .Solid(Color(0xff345288))
            }
            shapes {
                darkPixel = QrVectorPixelShape
                    .RoundCorners(.5f)
                ball = QrVectorBallShape
                    .RoundCorners(.25f)
                frame = QrVectorFrameShape
                    .RoundCorners(.25f)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            editInput.setText(R.string.app_name)

            ivQrcode
                .setImageBitmap(
                    QrCodeDrawable( { editInput.text.toString() }, options)
                        .toBitmap(1024, 1024),
                )

            editInput.addTextChangedListener {
                val text = editInput.text.toString()

                ivQrcode.setImageDrawable(
                    QrCodeDrawable( { text }, options),
                )
            }
            btnCreate.setOnClickListener {
                ivQrcode.setImageDrawable(
                    QrCodeDrawable(
                        {
                            editInput.text.toString()
                        },
                        options
                    ),
                )
            }
        }
    }
}