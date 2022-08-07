# Сustom QR generator for Android
Android library for creating QR-codes with logo, custom pixel/eyes shapes, background image. Powered by <a href="https://github.com/zxing/zxing">ZXing</a>.

<table>
  <tr>
    <td><img src="./screenshots/telegram.png" width="256" height="256"></td>
    <td><img src="./screenshots/github.png" width="256" height="256"></td>
    <td><img src="./screenshots/tiktok.png" width="256" height="256"></td>
  </tr>
</table>


## Installation
[![](https://jitpack.io/v/alexzhirkevich/custom-qr-generator.svg)](https://jitpack.io/#alexzhirkevich/custom-qr-generator)
[![](https://jitpack.io/v/alexzhirkevich/custom-qr-generator/month.svg)](https://jitpack.io/#alexzhirkevich/custom-qr-generator)
[![](https://jitpack.io/v/alexzhirkevich/custom-qr-generator/week.svg)](https://jitpack.io/#alexzhirkevich/custom-qr-generator)

[![buddy branch](https://app.buddy.works/sashazhirkevich/custom-qr-generator/repository/branch/main/badge.svg?token=f4939d609eb20130ce54bd87d6215d10e9fcd3d746eb5723428dae2181e3fd3e "buddy branch")](https://app.buddy.works/sashazhirkevich/custom-qr-generator/repository/branch/main)
<br>To get a Git project into your build:

<b>Step 1.</b> Add the JitPack repository to your build file
```gradle
allprojects {
    repositories {
      ...
        maven { url 'https://jitpack.io' }
    }
}
```
Or for gradle 7+ to settings.gradle file:
```gradle
dependencyResolutionManagement {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

<b>Step 2.</b> Add the dependency.
```gradle
dependencies {
    implementation 'com.github.alexzhirkevich:custom-qr-generator:1.2.5'
}
```


## Usage

<b>Step 1.</b> Create QR code data. There are multiple QR types: Plain Text, Url, Wi-Fi,
Email, GeoPos, Profile Cards, Phone, etc.

```kotlin
val data = QrData.Url("https://example.com")
```

<b>Step 2.</b> Define styling options:

```kotlin
val options = QrOptions.Builder(1024)
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
```

<b>Step 3.</b> Create a QR code generator and pass your data and options into it:

```kotlin  
val generator: QrCodeGenerator = QrGenerator()
  
val bitmap = generator.generateQrCode(data, options)
```

### DSL

```QrOptions``` can be created via Kotlin DSL. This also allows to easily create custom shape for
QR elements by drawing on canvas using ```drawShape``` function. This is extension function
for ```QrOptions.Builder``` and can be used not only inside ```createQrOptions```

For example:

<table align="center-vertical">
<td>
<img src="./screenshots/ring.png" width="256" height="256">
</td>
<td>

```kotlin  
 val options = createQrOptions(1024) {
        elementsShapes = QrElementsShapes(
            darkPixel = drawShape { canvas, drawPaint, erasePaint ->
                val cx = canvas.width/2f
                val cy = canvas.height/2f
                val radius = minOf(canvas.width, canvas.height)/2f
                canvas.drawCircle(cx, cy,radius, drawPaint)
                canvas.drawCircle(cx, cy,radius*2/2.5f, erasePaint)
                canvas.drawCircle(cx, cy,radius/1.75f, drawPaint)
            }.asPixelShape()
        )
    }
```

</td>
</table>

### Multi-threading

It is better to perform QR codes generating in background thread.
Generator supports cancellation with coroutines.

```kotlin  
val bitmap = generator.generateQrCodeSuspend(data, options)
```

Generator can work in parallel threads (different Default coroutine dispatchers).
<br><b>NOTE: Use wisely! More threads doesn't mean more performance!</b>
It depends on device and size of the QR code.<br>By default generator works in SingleThread.
To change it pass another ```QrGenerator.ThreadPolicy``` to ```QrGenerator``` constructor.<br>
For example:

```kotlin
val threadPolicy = when(Runtime.getRuntime().availableProcessors()){
    in 1..3 -> QrGenerator.ThreadPolicy.SingleThread
    in 4..6 -> QrGenerator.ThreadPolicy.DoubleThread
    else -> QrGenerator.ThreadPolicy.QuadThread
}

val generator: QrCodeGenerator = QrGenerator(threadPolicy)

```

## Customization

You can easily implement your own shapes and coloring for QR Code elements using math formulas or by drawing on canvas.

<table align="center-vertical">
<tr>
  <td>
  <img src="./screenshots/circlepixels.png" width="256" height="256">
  </td>
  <td>

  ```kotlin
  object Circle : QrPixelShape {
      override fun invoke(
          i: Int, j: Int, elementSize: Int,
          qrPixelSize: Int, neighbors: Neighbors
      ): Boolean {
          val center = elementSize/2.0
          return sqrt((center-i)*(center-i) + 
              (center-j)*(center-j)) < center
      }
  }
  ```
  </td>
</tr>
<tr>
  <td>
  <img src="./screenshots/ring.png" width="256" height="256">
  </td>
  <td>

  ```kotlin  
 object Ring : QrCanvasShapeModifier {
        override fun draw(canvas: Canvas, drawPaint: Paint, erasePaint: Paint) {
            val cx = canvas.width/2f
            val cy = canvas.height/2f
            val radius = minOf(canvas.width, canvas.height)/2f
            canvas.drawCircle(cx, cy,radius, drawPaint)
            canvas.drawCircle(cx, cy,radius*2/2.5f, erasePaint)
            canvas.drawCircle(cx, cy,radius/1.75f, drawPaint)
        }
    }

  val ring : QrPixelShape = Ring.toShapeModifier(1024).asPixelShape()
  
  ```
Or use DSL ```createQrOptions``` with ```drawShape``` function

  </td>
</tr>
</table>

