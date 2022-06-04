# custom-qr-generator
Android library for creating QR-codes with logo, custom pixel/eyes shapes, background image. Powerd by <a href="https://github.com/zxing/zxing">ZXing</a>.

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
    implementation 'com.github.alexzhirkevich:custom-qr-generator:1.0.1'
}
```
<table>
  <tr>
    <td><img src="./screenshots/telegram.bmp" width="256" height="256"></td>
    <td><img src="./screenshots/github.bmp" width="256" height="256"></td>
        <td><img src="./screenshots/snapchat.bmp" width="256" height="256"></td>

  </tr> 
<table>
  
To create a QR Code Bitmap, first define the styling options:
  
```kotlin
val options = QrOptions.Builder(1024)
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
          frame = QrFrameStyle.RoundedCorners(.3f),
          shape = QrShape.RoundCorners(.1f)
      )
  )
  .build()
```
Then create a QR code generator and pass your text and options into it (it is better to perform this in background thread):
  
```kotlin  
val generator : QrCodeCreator = QRGenerator()
  
val bitmap = generator.createQrCode("Your text here", options)
```


