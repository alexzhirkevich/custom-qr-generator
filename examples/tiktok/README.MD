<table>
    <tr>
        <td>
            Original
        </td>
        <td>
            Generated
        </td>
    </tr>
    <tr>
        <td>
            <img width="250" height="450" src="/examples/tiktok/original.jpeg">
        </td>
        <td>
             <img idth="250" height="250" src="/examples/tiktok/generated.jpeg">
        </td>
    </tr>
</table>

```kotlin
val options = createQrOptions(1024,1024,.1f) {
    shapes {
        darkPixel = QrPixelShape.Circle(.8f)
        ball = drawShape { canvas, drawPaint, _ ->
            canvas.drawCircle(
                canvas.width / 2f, canvas.height / 2f,
                canvas.width.toFloat() * .4f, drawPaint
            )
            canvas.drawCircle(
                canvas.width * .4f, canvas.height / 2f,
                canvas.width.toFloat() * .4f, drawPaint
            )
            canvas.drawCircle(
                canvas.width * .6f, canvas.height / 2f,
                canvas.width.toFloat() * .4f, drawPaint
            )
        }
        frame = QrFrameShape.Circle(.4f, .8f)
    }

    colors {
        ball = draw {
            val paint = Paint().apply {
                isAntiAlias = true
            }
            drawCircle(width * .4f, height / 2f,
                width.toFloat() * 0.4f - 1, Paint().apply { color = 0xff00f2ea.toColor() })
            drawCircle(width * .6f, height / 2f,
                width.toFloat() * 0.4f - 1, Paint().apply { color = 0xffff0050.toColor() })
            drawCircle(width / 2f, height / 2f,
                width.toFloat() * 0.4f - 1, Paint().apply { color = 0xff000000.toColor() })
        }
        dark = QrColorSeparatePixels.Random(
            mapOf(
                0xffff0050.toColor() to .025f,
                0xff00f2ea.toColor() to .025f,
                0xff000000.toColor() to 1f
            )
        )
        frame = QrColor.Solid(0xff000000.toColor())
    }
    background {

        color = draw {
            val size = maxOf(width, height)
            val pad = size * 0.025f
            val stroke = size * 0.01f
            val width = size - pad * 2
            val whitePaint = Paint().apply {
                color = 0xffffffff.toColor()
            }

            drawRect(0f, 0f, this.width.toFloat(),
                this.height.toFloat(), whitePaint)


            fun frame(color: Int) {
                drawRoundRect(
                    pad, pad, pad + width,
                    pad + width, pad * 2,
                    pad * 2, Paint()
                        .apply {
                            this.color = color
                            strokeWidth = stroke
                            this.style = Paint.Style.STROKE
                        }
                )
            }
            withTranslation(-stroke/2, -stroke/2) {
                frame(0xff00f2ea.toColor())
            }
            withTranslation(stroke/2, stroke/2) {
                frame(0xffff0050.toColor())
            }
            frame(0xff000000.toColor())
            withRotation(45f, this.width/2f, this.height/2f){
                drawRect(-this.width * .1f,-this.height * .1f,
                    this.width * 1.1f , this.height * 1.1f,
                   whitePaint)
            }
        }
    }
}
```
