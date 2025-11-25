package ch.sebpiller.easy.lotto.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Rect
import android.util.Log
import androidx.core.graphics.createBitmap
import com.google.mlkit.common.sdkinternal.MlKitThreadPool
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import androidx.core.graphics.scale

class ImageReader() {
    data class GridPosition(val row: Int, val col: Int) {
        init {
            require(row in 0..2) { "row must be 0..2" }
            require(col in 0..8) { "col must be 0..8" }
        }
    }

    data class DetectedNumber(
        val value: Int, val position: GridPosition, val bbox: Rect? = null
    )


    private fun preprocessImage(bitmap: Bitmap): Bitmap {
        val resized = resizeToAtMost(bitmap, 480, 320)

        val width = resized.width
        val height = resized.height
        val processedBitmap = createBitmap(width, height)
        val canvas = Canvas(processedBitmap)
        val paint = android.graphics.Paint()

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)

        colorMatrix.set(
            floatArrayOf(
                2f, 0f, 0f, 0f, -128f,
                0f, 2f, 0f, 0f, -128f,
                0f, 0f, 2f, 0f, -128f,
                0f, 0f, 0f, 1f, 0f
            )
        )

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(resized, 0f, 0f, paint)

        return DarkContentExtractor().extract(processedBitmap)
    }

    fun resizeToAtMost(bitmap: Bitmap, maxWidth: Int = 480, maxHeight: Int = 320): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Nothing to do if already small enough
        if (width <= maxWidth && height <= maxHeight) return bitmap

        // Compute a uniform scale to fit within the bounds while preserving aspect ratio
        val scale = minOf(
            maxWidth.toFloat() / width.toFloat(), maxHeight.toFloat() / height.toFloat()
        )

        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)

        return bitmap.scale(targetWidth, targetHeight)
    }

    suspend fun read(input: InputImage): MutableList<DetectedNumber> {
        val processedBitmap = preprocessImage(input.bitmapInternal!!)
        val cellHeight = processedBitmap.height / 3f
        val cellWidth = processedBitmap.width / 9f
        val results = mutableListOf<DetectedNumber>()
        val recognized = recognize(InputImage.fromBitmap(processedBitmap, 0)) ?: return mutableListOf<DetectedNumber>()

        for (block in recognized.textBlocks) {
            for (line in block.lines) {
                if (line.angle > 10 || line.angle < -10) continue

                for (element in line.elements) {
                    val text = element.text.trim()
                    Log.d("ImageReader", "Found raw text: $text")

                    // if (element.confidence < 0.5) continue
                    // if(element.boundingBox!!.width() < (image.width*0.5)) continue
                    val bb = element.boundingBox!!
                    val minHeight =
                        processedBitmap.height * 0.333 * 0.25 // each number must be at least 25% height of a cell
                    if (bb.height() < minHeight) continue

                    val row = ((bb.top / cellHeight).toInt())
                    val from = (((bb.left + 10) / cellWidth).toInt())

                    if (bb.width() <= (processedBitmap.width / 9f)) {
                        val num = text.toIntOrNull()
                        if (num != null) {
                            results.add(
                                DetectedNumber(
                                    value = num, position = GridPosition(row, from), bbox = bb
                                )
                            )
                        }
                    } else {
                        val to = ((bb.left + bb.width() - 10) / cellWidth).toInt()
                        Log.i("ImageReader", "Found text overlapping several cells at $row, $from-$to")

                        for (i in from..to) {
                            val subImage = Bitmap.createBitmap(
                                processedBitmap,
                                (i * cellWidth).toInt(),
                                (row * cellHeight).toInt(),
                                cellWidth.toInt(),
                                cellHeight.toInt()
                            )

                            val subRecognized = recognize(InputImage.fromBitmap(subImage, 0))
                            val text = subRecognized!!.textBlocks.map { block ->
                                block.lines.filter { line -> line.angle < 10 && line.angle > -10 }
                                    .filter { line -> line.boundingBox!!.height() > minHeight }
                                    .map { line -> line.text }
                            }.flatten().joinToString(" ")
                            if (text.isBlank()) continue

                            val num = text.toIntOrNull()
                            if (num != null) {
                                results.add(DetectedNumber(num, GridPosition(row = row, col = i), bb))
                            }
                        }
                    }
                }
            }
        }

        // Deduplicate per cell: keep the largest bbox (most likely the big number) if multiple
        val byCell = results.groupBy { it.position }
        val deduped = byCell.values.mapNotNull { candidates ->
            candidates.maxByOrNull { it.bbox?.width()?.toLong()?.times(it.bbox.height().toLong()) ?: 0 }
        }

        Log.d("ImageReader", "Detected ${results.size} numbers")

        val res = deduped.toMutableList();
        res.sortBy { it.value }
        return res
    }

    companion object {
        private val recognizer by lazy {
            TextRecognition.getClient(
                TextRecognizerOptions.Builder().setExecutor(MlKitThreadPool()).build()
            )
        }
    }

    var i = 0
    suspend fun recognize(image: InputImage): Text? = suspendCancellableCoroutine { cont ->
        i++
        recognizer.process(image).addOnSuccessListener { cont.resume(it) }.addOnFailureListener {
                Log.e("ImageReader", "Text recognition failed", it)
                cont.resume(null)
            }
        Log.i("ImageReader", "!!!! Image recognition success: $i")
    }
}