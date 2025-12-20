package ch.sebpiller.easy.lotto.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

data class CropRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

@Composable
fun ImageCropperScreen(
    bitmap: Bitmap,
    onCropConfirmed: (croppedBitmap: Bitmap) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier
) {
    var cropRect by remember {
        mutableStateOf(
            CropRect(
                left = 0.2f,
                top = 0.2f,
                right = 0.8f,
                bottom = 0.8f
            )
        )
    }



    fun cropBitmap(bitmap: Bitmap, cropRect: CropRect): Bitmap {
        val left = (bitmap.width * cropRect.left).toInt().coerceIn(0, bitmap.width - 1)
        val top = (bitmap.height * cropRect.top).toInt().coerceIn(0, bitmap.height - 1)
        val width = ((bitmap.width * cropRect.right).toInt() - left).coerceAtLeast(1)
        val height = ((bitmap.height * cropRect.bottom).toInt() - top).coerceAtLeast(1)

        return Bitmap.createBitmap(bitmap, left, top, width, height)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        Text(
            text = "Recadrer l'image",
            color = Color.Blue,
            modifier = Modifier.padding(16.dp)
        )


        Surface(modifier = modifier) {

            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Image à recadrer",
                modifier = modifier
                    .fillMaxWidth().aspectRatio(4f / 3)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaX = dragAmount.x / size.width
                            val deltaY = dragAmount.y / size.height

                            cropRect = cropRect.copy(
                                left = (cropRect.left + deltaX).coerceIn(0f, cropRect.right - 0.1f),
                                top = (cropRect.top + deltaY).coerceIn(0f, cropRect.bottom - 0.1f)
                            )
                        }
                    },
                contentScale = ContentScale.Fit
            )


            // Overlay de recadrage
            Canvas(
                modifier = Modifier
                    .fillMaxWidth().aspectRatio(4f / 3)
            ) {
                val cropRectPx = Rect(
                    left = cropRect.left * size.width,
                    top = cropRect.top * size.height,
                    right = cropRect.right * size.width,
                    bottom = cropRect.bottom * size.height
                )

                // Overlay semi-transparent en dehors de la zone
                drawRect(
                    color = Color.Black.copy(alpha = 0.6f),
                    size = size
                )

                // Découpe transparente
                drawRect(
                    color = Color.Transparent,
                    topLeft = Offset(cropRectPx.left, cropRectPx.top),
                    size = Size(
                        cropRectPx.width,
                        cropRectPx.height
                    )
                )

                // Bordure du rectangle de recadrage
                drawRect(
                    color = Color.Green,
                    topLeft = Offset(cropRectPx.left, cropRectPx.top),
                    size = Size(
                        cropRectPx.width,
                        cropRectPx.height
                    ),
                    style = Stroke(width = 2f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(onClick = onCancel) {
                Text("Annuler")
            }

            Button(onClick = {
                val croppedBitmap = cropBitmap(bitmap, cropRect)
                onCropConfirmed(croppedBitmap)
            }) {
                Text("Recadrer")
            }
        }
    }

}