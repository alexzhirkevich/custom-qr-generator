# custom-qr-generator
Android library for creating QR-codes with logo, custom pixel/eyes shapes, background image. Powerd by <a href="https://github.com/zxing/zxing">ZXing</a>.

<table>
  <tr>
    <td><img src="./screenshots/telegram.png" width="256" height="256"></td>
    <td><img src="./screenshots/github.png" width="256" height="256"></td>
    <td><img src="./screenshots/tiktok.png" width="256" height="256"></td>

<!--   </tr>  -->
<!--     <td><img src="./screenshots/snapchat.png" width="256" height="256"></td> -->
<!--   </tr>  -->
<table>
  
<!-- 
## Installation
[![](https://jitpack.io/v/alexzhirkevich/custom-qr-generator.svg)](https://jitpack.io/#alexzhirkevich/custom-qr-generator)

To get a Git project into your build:

<b>Step 1.</b> Add the JitPack repository to your build file
```gradle
allprojects {
    repositories {
      ...
        maven { url 'https://jitpack.io' }
    }
}
```
<b>Step 2.</b> Add the dependency
```gradle
dependencies {
    implementation 'com.github.alexzhirkevich:custom-qr-generator:1.0.0'
}
```
 -->
 
## Usage

To create a QR Code Bitmap, first define styling options:
  
```kotlin
val options = QrOptions.Builder(1024)
    .setPadding(150)
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
            pixel = QrPixelStyle.RoundCorners(),
            ball = QrBallStyle.RoundCorners(.3f),
            frame = QrFrameStyle.RoundCorners(.3f),
            bgShape  = QrBackgroundStyle.RoundCorners(.1f)
        )
    )
    .build()
```
Then create a QR code generator and pass your text and options into it (it is better to perform this in background thread):
  
```kotlin  
val generator : QrCodeCreator = QRGenerator()
  
val bitmap = generator.createQrCode("Your text here", options)
```
## Customization
  
You can easily implement your own shapes for QR Code elements using math formula to decide if bitmap pixel needs to be dark.
For example, this is implementation of Circle QR-pixels:
  
<img src="./screenshots/circlepixels.png" width="256" height="256">
 
```kotlin
object Circle : QrPixelStyle {
    override fun isDark(
        i: Int, j: Int, elementSize: Int,
        pixelSize: Int, neighbors: Neighbors
    ): Boolean {
        val center = elementSize/2.0
        return (sqrt((center-i)*(center-i) + (center-j)*(center-j)) < center)
    }
}
```

