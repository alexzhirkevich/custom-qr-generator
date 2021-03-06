# custom-qr-generator
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
Or for gradle 7+ to settings.gradle file: 
```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven {
            url 'https://jitpack.io'        
        }
    }
}
```

<b>Step 2.</b> Add the dependency.
```gradle
dependencies {
    implementation 'com.github.alexzhirkevich:custom-qr-generator:1.2.3'
}
```

 
## Usage

<b>Step 1.</b> Create QR code data. There are multiple QR types support: Plain Text, Url, Wi-Fi,
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

It is better to perform QR codes generating in background thread.
Generator supports cancellation with coroutines.

```kotlin  
val bitmap = generator.generateQrCodeSuspend(data, options)
```

## Customization
  
You can easily implement your own shapes and coloring for QR Code elements using math formulas.
For example, this is implementation of Circle QR-pixels:
  
<img src="./screenshots/circlepixels.png" width="256" height="256">
 
```kotlin
object Circle : QrPixelShape {
    override fun invoke(
        i: Int, j: Int, elementSize: Int,
        qrPixelSize: Int, neighbors: Neighbors
    ): Boolean = {
        val center = elementSize/2.0
        return (sqrt((center-i)*(center-i) + (center-j)*(center-j)) < center)
    }
}
```

