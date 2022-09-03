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
            <img width="250" height="450" src="/examples/intagram/original.jpeg">
        </td>
        <td>
             <img width="250" height="250" src="/examples/intagram/generated.jpeg">
        </td>
    </tr>
</table>

```kotlin
 class CircleRandomRadius : QrPixelShape {

    private val shapes = listOf(
        QrPixelShape.Circle(.6f),
        QrPixelShape.Circle(.75f),
        QrPixelShape.Circle(.9f),
    )

    private var lastNeighbors = Neighbors.Empty
    private var lastShape = shapes.random()

    override fun invoke(i: Int, j: Int, elementSize: Int, neighbors: Neighbors): Boolean {
        if (lastNeighbors != neighbors) {
            lastNeighbors = neighbors
            lastShape = shapes.random()
        }
        return lastShape.invoke(i, j, elementSize, neighbors)
    }
}

private val inst = createQrOptions(1024, 1024, .1f){
    colors {
        dark = QrColor.LinearGradient(
            startColor = 0xffbc077a.toColor(),
            endColor = 0xff9a10a4.toColor(),
            orientation = QrColor.LinearGradient.Orientation.LeftDiagonal
        )
    }

    shapes {
        frame = QrFrameShape.RoundCorners(.3f)
        ball = QrBallShape.Circle()
        darkPixel = CircleRandomRadius()
    }

    logo {
        drawable = DrawableSource.Resource(R.drawable.iglogo)
        padding = QrLogoPadding.Natural(.05f)
        shape = QrLogoShape.RoundCorners(.3f)
        size = .3f
    }
}
```
