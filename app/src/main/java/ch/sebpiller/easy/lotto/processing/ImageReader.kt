package ch.sebpiller.easy.lotto.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.math.floor

/**
 * Reads a lotto grid image and extracts the 15 large numbers and their positions.
 * The grid is 9 columns by 3 rows. We map each detected number's bounding box center
 * to a cell in this grid.
 */
class ImageReader {

    data class GridPosition(val row: Int, val col: Int) {
        init {
            require(row in 0..2) { "row must be 0..2" }
            require(col in 0..8) { "col must be 0..8" }
        }
    }

    data class DetectedNumber(
        val value: Int,
        val position: GridPosition,
        val bbox: Rect
    )

    /**
     * Process the image pointed by [uri] and return the list of detected numbers with their grid positions.
     */
    suspend fun read(image: InputImage): List<DetectedNumber> {
        val visionText = recognize(image) ?: return emptyList()

        val results = mutableListOf<DetectedNumber>()

        // Divide the image space
        val cellWidth = image.width / 9.0
        val cellHeight = image.height / 3.0

        // Iterate through text elements (fine-grained boxes)
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    val text = element.text.trim()
                    val number = text.toIntOrNull() ?: continue
                    // Typically lotto numbers are 1..90, filter anything absurd
                    if (number !in 1..99) continue

                    val box: Rect = element.boundingBox ?: continue
                    val center = Point(box.centerX(), box.centerY())

                    val col = floor(center.x / cellWidth).toInt().coerceIn(0, 8)
                    val row = floor(center.y / cellHeight).toInt().coerceIn(0, 2)

                    results.add(
                        DetectedNumber(
                            value = number,
                            position = GridPosition(row, col),
                            bbox = Rect(box)
                        )
                    )
                }
            }
        }

//        // Deduplicate per cell: keep the largest bbox (most likely the big number) if multiple
     //   val byCell = results.groupBy { it.position }
//        val deduped = byCell.values.mapNotNull { candidates ->
//            candidates.maxByOrNull { it.bbox.width().toLong() * it.bbox.height().toLong() }
//        }

        Log.d("ImageReader", "Detected ${results.size} numbers")
        return results
    }

    fun loadBitmap(context: Context, uri: Uri): Bitmap? = try {
        val stream: InputStream? = context.contentResolver.openInputStream(uri)
        stream.use { BitmapFactory.decodeStream(it) }
    } catch (t: Throwable) {
        Log.e("ImageReader", "Failed to load bitmap: $uri", t)
        null
    }

    suspend fun recognize(image: InputImage): Text? = suspendCancellableCoroutine { cont ->
        TextRecognition.getClient(DEFAULT_OPTIONS)
            .process(image)
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener {
                Log.e("ImageReader", "Text recognition failed", it)
                cont.resume(null)
            }
    }
}