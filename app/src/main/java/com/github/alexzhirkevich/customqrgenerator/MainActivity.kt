package com.github.alexzhirkevich.customqrgenerator

import android.graphics.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.minus
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.github.alexzhirkevich.customqrgenerator.example.R
import com.github.alexzhirkevich.customqrgenerator.example.databinding.ActivityMainBinding
import com.github.alexzhirkevich.customqrgenerator.style.*
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.createQrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.*
import kotlinx.coroutines.delay
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

@ExperimentalSerializationApi
class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    val options = createQrVectorOptions {

        padding = .325f

        background {
            drawable = DrawableSource
                .Resource(R.drawable.frame)
        }

        logo {
            drawable = DrawableSource
                .Resource(R.drawable.tg)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            editInput.setText(R.string.app_name)

            ivQrcode
                .setImageBitmap(
                    QrCodeDrawable(this@MainActivity, { editInput.text.toString() }, options)
                        .toBitmap(1024, 1024),
                )

            editInput.addTextChangedListener {
                val text = editInput.text.toString()

                ivQrcode.setImageDrawable(
                    QrCodeDrawable(this@MainActivity, { text }, options),
                )
            }
            btnCreate.setOnClickListener {
                ivQrcode.setImageDrawable(
                    QrCodeDrawable(
                        this@MainActivity,
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