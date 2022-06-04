# custom-qr-generator
Android library for creating QR-codes with logo, custom pixel/eyes shapes, background image. Powerd by <a href="https://github.com/zxing/zxing">ZXing</a>

<!-- <img src="./screenshots/telegram.bmp" width="256" height="256"> -->
<!-- <img src="./screenshots/github.bmp" width="256" height="256"> -->
<table>
  <tr>
    <td><img src="./screenshots/telegram.bmp" width="256" height="256"></td>
    <td><img src="./screenshots/github.bmp" width="256" height="256"></td>
  </tr> 
<table>
  
```kotlin
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
          ball = QrBallStyle.RoundCorners(
              corner = .25f,
              horizontalOuter = false,
              verticalOuter = false
          ),
          frame = QrFrameStyle.RoundedCorners(
              corner = .25f,
              horizontalOuter = false,
              verticalOuter = false
          ),
          shape = QrShape.RoundCorners(.1f)
      )
  )
  .build()
```
