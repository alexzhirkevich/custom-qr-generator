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
    implementation 'com.github.alexzhirkevich:custom-qr-generator:1.3.0'
}
```


## Usage

<b>Step 1.</b> Create QR code data. There are multiple QR types: Plain Text, Url, Wi-Fi,
Email, GeoPos, Profile Cards, Phone, etc.

```kotlin
val data = QrData.Url("https://example.com")
```

<b>Step 2.</b> Define styling options using builder:

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
            size = .25f,
            padding = QrLogoPadding.Accurate(.2f),
            shape = QrLogoShape
                .Circle
        )
    )
    .setColors(
        QrColors(
            dark = QrColor
                .Solid(Color(0xff345288)),
            highlighting = QrColor
                .Solid(Color(0xddffffff)),
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
            highlighting = QrBackgroundShape
                .RoundCorners(.05f)
        )
    )
    .build()
```

Or using DSL :

```kotlin
val options = createQrOptions(1024, .3f) {
    backgroundImage = QrBackgroundImage(
    // ...
    )
    logo = QrLogo(
        // ...
    )
}
```

```Color``` function takes 0xAARRGGBB long and converts it to color int. 
There is also ```Long.toColor()``` function.

<b>Step 3.</b> Create a QR code generator and pass your data and options into it:

```kotlin  
val generator: QrCodeGenerator = QrGenerator()
  
val bitmap = generator.generateQrCode(data, options)
```
‼️ QR codes must be generated in background thread. Generator supports cancellation with coroutines.

```kotlin  
//todo: don't use GlobalScope
GlobalSope.launch {
    val bitmap = generator.generateQrCodeSuspend(data, options)
}
```

Generator can work in parallel threads (different Default coroutine dispatchers). 
By default generator works in SingleThread. To change it pass another ```QrGenerator.ThreadPolicy``` to
```QrGenerator``` constructor.

For example:

```kotlin
val threadPolicy = when(Runtime.getRuntime().availableProcessors()){
    in 1..3 -> QrGenerator.ThreadPolicy.SingleThread
    in 4..6 -> QrGenerator.ThreadPolicy.DoubleThread
    else -> QrGenerator.ThreadPolicy.QuadThread
}

val generator: QrCodeGenerator = QrGenerator(threadPolicy)

```

‼️ <b>NOTE: Use wisely! More threads doesn't mean more performance!</b> It depends on device 
and size of the QR code.

## Customization


You can easily implement your own shapes and coloring for QR Code in 2 ways: 
using math formulas or by drawing on canvas. Second way is usually slower 
(it takes more time to build ```QrOptions```, but code generating is a 
little bit faster) and uses a lot of memory but provides more freedom.

For example:

1. Using math formulas:
<table>
<tr>
<td>
<img src="./screenshots/circlepixels.png" width="230" height="230">
</td>
<td>

```kotlin
object Circle : QrPixelShape {
    override fun invoke(
        i: Int, j: Int, elementSize: Int,
        qrPixelSize: Int, neighbors: Neighbors
    ): Boolean {
        val center = elementSize/2.0
        return sqrt((center-i).pow(2) + (center-j).pow(2)) < center
    }
}

val options = createQrOptions(1024, .3f) {
    elementsShapes = QrElementsShapes(
        darkPixel = Circle
    )
}
```

</td>
</tr>
<tr>
<td>
<img src="./screenshots/pride.png" width="230" height="230">

</td>
<td>

```kotlin

//It is not scannable. Don't create such colorful QR codes
object Pride : QrColor {
    override fun invoke(
        i: Int, j: Int, elementSize: Int, qrPixelSize: Int
    ): Int {
        return when(6f * j/elementSize){
            in 0f..1f -> Color.RED
            in 1f..2f-> Color(0xffffa500)
            in 2f..3f-> Color.YELLOW
            in 3f..4f-> Color(0xff00A300)
            in 4f..5f-> Color.BLUE
            else -> Color(0xff800080)
        }
    }
}

val options = createQrOptions(1024) {
    colors = QrColors(
        ball = Pride
    )
}
```

</td>
</tr>
</table>

2. By drawing on canvas:

<table>
<td>
<img src="./screenshots/ring.png" width="230" height="230">
</td>
<td>

```kotlin  
val options : QrOptions = createQrOptions(1024) {
  elementsShapes = QrElementsShapes(
      darkPixel = drawElementShape { canvas, drawPaint, erasePaint ->
          val cx = canvas.width/2f
          val cy = canvas.height/2f
          val radius = minOf(cx,cy)
          canvas.drawCircle(cx, cy, radius, drawPaint)
          canvas.drawCircle(cx, cy, radius*2/2.5f, erasePaint)
          canvas.drawCircle(cx, cy, radius/1.75f, drawPaint)
      }
  )
}
```
</td>
</table>

```drawElementFunction``` is a generic function that can be used only inside a 
```QrOptionsBuilderScope``` and only to create properties of QrElementsShapes

‼️ <b>NOTE: Created shape should not be used with other ```QrOptions``` with larger size!</b> 
This can cause shape quality issues.

You can also implement ```QrCanvasShapeModifier``` and cast it so necessary shape:

```kotlin  
object Ring : QrCanvasShapeModifier {
   override fun draw(
       canvas: Canvas, drawPaint: Paint, erasePaint: Paint
   ) {
       // ...
   }
}

val ring : QrPixelShape = Ring
    .toShapeModifier(elementSize = 48)
    .asPixelShape()
// or automatically determine size with DSL
val ringPixelOptions : QrOptions = createQrOptions(1024){
    elementsShapes = QrElementsShapes(
        darkPixel = drawElementShape(Ring::draw)
    )
}
```

<table>
<td>
<img src="./screenshots/canvascolor.png" width="230" height="230">
</td>
<td>

```kotlin  
object CanvasColor : QrCanvasColor {
    override fun draw(canvas: Canvas) = with(canvas) {
        withRotation(135f, width/2f, height/2f) {
            drawRect(-width / 2f, -height / 2f,
                1.5f * width, height / 2f,
                Paint().apply { color = Color.BLACK }
            )
            drawRect(-width / 2f, height / 2f, 
                1.5f * width.toFloat(), 1.5f * height.toFloat(),
                Paint().apply { color = Color.DKGRAY }
            )
        }
    }
}
   
```
</td>
</table>

Using ```draw``` function inside ```QrOptionsBuilderScope``` you can colorize 
your code elements as you want. It will be converted to a ```QrColor```.

This is ```QrOptions``` of the code above:

```kotlin
val options =  createQrOptions(1024, .2f) {
    colors = QrColors(
        dark = QrColor.RadialGradient(
            startColor = Color.GRAY,
            endColor = Color.BLACK
        ),
        ball = draw(CanvasColor::draw),
        frame = draw {
            withRotation(180f, width/2f, 
              height/2f, CanvasColor::draw)
        },
        symmetry = true
    )
    elementsShapes = QrElementsShapes(
        darkPixel = QrPixelShape.RoundCorners(),
        frame = QrFrameShape.RoundCorners(.25f,
        outer = false, inner = false)
    )
}
```

‼️ NOTE: Created color should not be used with other QrOptions with larger size! 


