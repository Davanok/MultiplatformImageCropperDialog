package // TODO

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.fromByteArray
import example.toByteArray
import example.composeapp.generated.resources.Res
import example.composeapp.generated.resources.cancel
import example.composeapp.generated.resources.character_image
import example.composeapp.generated.resources.discard_changes
import example.composeapp.generated.resources.finish
import example.composeapp.generated.resources.restart_alt
import example.composeapp.generated.resources.rotate_90_degrees_cw
import example.composeapp.generated.resources.rotate_left
import example.composeapp.generated.resources.rotate_right
import example.composeapp.generated.resources.zoom_in
import example.composeapp.generated.resources.zoom_out
import example.composeapp.generated.resources.remove
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sin

private fun ImageBitmap.intSize() = IntSize(width, height)
private fun ImageBitmap.size() = Size(width.toFloat(), height.toFloat())
private val ImageBitmap.minSize
    get() = min(width, height)

private operator fun Size.minus(other: Float) = Size(width - other, height - other)
private fun Size.swap() = Size(height, width)

private fun Offset.toIntOffset() = IntOffset(x.toInt(), y.toInt())

private fun Float.toRadians(): Float = (this * PI / 180).toFloat()

@Stable private fun Modifier.mirrorHorizontal() = scale(scaleX = -1f, scaleY = 1f)

private fun checkImageOffset(currentOffset: Offset, offsetChange: Offset, imageMinusBox: Size): Offset{
    val futureOffset = currentOffset + offsetChange
    val futureOffsetX2 = futureOffset * 2F
    return Offset(
        if (abs(futureOffsetX2.x) <= imageMinusBox.width) futureOffset.x else currentOffset.x,
        if (abs(futureOffsetX2.y) <= imageMinusBox.height) futureOffset.y else currentOffset.y
    )
}
private fun globalOffsetChange(offset: Offset, rotation: Float): Offset = when(rotation){
    90f -> Offset(-offset.y, offset.x)
    180f -> offset * -1f
    270f -> Offset(offset.y, -offset.x)
    else -> offset
}
private fun changeOffsetWhenScale(zoomChange: Float, imageRealSize: Size, boxSizePx: Float, offset: Offset): Offset {
    val oneMinusSizeChange = (1 - zoomChange)
    val offsetX2 = offset * 2f

    if (zoomChange >= 1) return offset * -oneMinusSizeChange
    return Offset(
        if (imageRealSize.width * zoomChange - abs(offsetX2.x) < boxSizePx)
            imageRealSize.width * oneMinusSizeChange / 2 * (-offset.x.sign)
        else -offset.x * oneMinusSizeChange,

        if (imageRealSize.height * zoomChange - abs(offsetX2.y) < boxSizePx)
            imageRealSize.height * oneMinusSizeChange / 2 * (-offset.y.sign)
        else -offset.y * oneMinusSizeChange,
    )
}

@Composable
fun ImageCropDialog(
    bytes: ByteArray,
    boxSize: Dp,
    onResult: (result: ByteArray?) -> Unit
) {
    val density = LocalDensity.current
    val boxSizePx = remember(density) {
        density.run { boxSize.toPx() }
    }
    val imageBitmap = remember(bytes) {
        ImageBitmap.fromByteArray(bytes).getResizedBitmap(boxSizePx.toInt())
    }

    Dialog(
        onDismissRequest = { onResult(null) },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box (
            modifier = Modifier.fillMaxSize()
        ) {
            var scale by remember { mutableFloatStateOf(boxSizePx / imageBitmap.minSize) }
            var rotation by remember { mutableFloatStateOf(0f) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            val imageDisplaySize by remember(scale, rotation) {
                derivedStateOf {
                    val size = imageBitmap.size() * scale
                    if (rotation % 180 == 0f) size else size.swap()
                }
            }

            fun calculateZoomChange(zoomChange: Float){
                if (imageBitmap.minSize * scale * zoomChange >= boxSizePx) {
                    offset += changeOffsetWhenScale(
                        zoomChange,
                        imageDisplaySize,
                        boxSizePx,
                        offset
                    )
                    scale *= zoomChange
                }
            }

            val state = rememberTransformableState { zoomChange, offsetChange, _ ->
                offset = checkImageOffset(
                    offset,
                    globalOffsetChange(offsetChange * scale, rotation),
                    imageDisplaySize - boxSizePx
                )
                calculateZoomChange(zoomChange)
            }

            Image(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        rotationZ = rotation
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .transformable(state = state),
                bitmap = imageBitmap,
                contentDescription = stringResource(Res.string.character_image)
            )

            Box(
                modifier = Modifier
                    .background(
                        Color.Gray.copy(alpha = .5f),
                        RoundedCornerShape(16.dp)
                    )
                    .size(boxSize)
                    .align(Alignment.Center)
            )

            RotateButtons(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                rotation = (rotation + it) % 360
            }
            ZoomButtons(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) { calculateZoomChange(it) }
            ControlButtons(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                onCancel = { onResult(null) },
                onClear = {
                    scale = 1f
                    rotation = 0f
                    offset = Offset.Zero
                },
                onResult = { onResult(cropImage(imageBitmap, scale, offset, rotation, boxSizePx)) }
            )
        }
    }
}

private fun ImageBitmap.getResizedBitmap(minSize: Int): ImageBitmap {
    val bitmapRatio = width.toFloat() / height.toFloat()
    val (newWidth, newHeight) = if (bitmapRatio > 1) {
        minSize to (minSize / bitmapRatio).toInt()
    } else {
        (minSize * bitmapRatio).toInt() to minSize
    }
    return ImageBitmap(newWidth, newHeight).also { scaledBitmap ->
        Canvas(scaledBitmap).apply {
            drawImageRect(
                image = this@getResizedBitmap,
                dstSize = IntSize(newWidth, newHeight),
                paint = Paint()
            )
        }
    }
}

private fun cropImage(
    source: ImageBitmap,
    scale: Float,
    offset: Offset,
    rotation: Float,
    boxSize: Float,
    targetImageSize: Int = (boxSize / scale).toInt()
): ByteArray {

    val imageSize = source.intSize()
    val resultImageSize = (boxSize / scale).toInt()
    val scaledOffset = (offset / scale).toIntOffset()
    val center = Offset(imageSize.width / 2f, imageSize.height / 2f)

    val rotationRadians = rotation.toRadians()

    val cos = -cos(rotationRadians)
    val sin = sin(rotationRadians)

    val targetImageOffset = Offset(
        scaledOffset.x * cos - scaledOffset.y * sin + center.x - resultImageSize / 2f,
        scaledOffset.x * sin + scaledOffset.y * cos + center.y - resultImageSize / 2f
    ).toIntOffset()

    val bitmap = ImageBitmap(targetImageSize, targetImageSize).also {
        Canvas(it).apply {
            save()
            translate(resultImageSize / 2f, resultImageSize / 2f)
            rotate(rotation)
            translate(-resultImageSize / 2f, -resultImageSize / 2f)

            drawImageRect(
                image = source,
                srcOffset = targetImageOffset,
                srcSize = IntSize(resultImageSize, resultImageSize),
                dstSize = IntSize(targetImageSize, targetImageSize),
                paint = Paint()
            )
            restore()
        }
    }
    return bitmap.toByteArray()
}
@Composable
private fun RotateButtons(
    modifier: Modifier = Modifier,
    onRotate: (Int) -> Unit
) {
    Row (
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = { onRotate(270) }) {
            Icon (
                modifier = Modifier.mirrorHorizontal(),
                painter = painterResource(Res.drawable.rotate_90_degrees_cw),
                contentDescription = stringResource(Res.string.rotate_left)
            )
        }
        IconButton(onClick = { onRotate(90) }) {
            Icon (
                painter = painterResource(Res.drawable.rotate_90_degrees_cw),
                contentDescription = stringResource(Res.string.rotate_right)
            )
        }
    }
}
@Composable
private fun ZoomButtons(
    modifier: Modifier = Modifier,
    onZoom: (Float) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = { onZoom(1.2f) }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(Res.string.zoom_in)
            )
        }
        IconButton(
            onClick = { onZoom(0.8f) }
        ) {
            Icon(
                painter = painterResource(Res.drawable.remove),
                contentDescription = stringResource(Res.string.zoom_out)
            )
        }
    }
}

@Composable
private fun ControlButtons(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onClear: () -> Unit,
    onResult: () -> Unit
) {
    Row (
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onCancel
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = stringResource(Res.string.cancel)
            )
        }
        IconButton(
            onClick = onClear
        ) {
            Icon(
                painter = painterResource(Res.drawable.restart_alt),
                contentDescription = stringResource(Res.string.discard_changes)
            )
        }
        IconButton(
            onClick = onResult
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(Res.string.finish)
            )
        }
    }
}

