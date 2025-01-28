// commonMain

import androidx.compose.ui.graphics.ImageBitmap

expect fun ImageBitmap.Companion.fromByteArray(bytes: ByteArray): ImageBitmap

// android

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun ImageBitmap.Companion.fromByteArray(bytes: ByteArray) =
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()

// other (desktop, ios)

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun ImageBitmap.Companion.fromByteArray(bytes: ByteArray) =
    Image.makeFromEncoded(bytes).toComposeImageBitmap()
