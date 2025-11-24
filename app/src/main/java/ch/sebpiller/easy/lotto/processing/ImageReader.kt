package ch.sebpiller.easy.lotto.processing

import android.graphics.Point
import android.graphics.Rect
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.floor
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.imgproc.Imgproc

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

    suspend fun read(image: InputImage): List<DetectedNumber> {
        val visionText = recognize(image) ?: return emptyList()

        val results = mutableListOf<DetectedNumber>()


        // Iterate through text elements (fine-grained boxes)
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                if(line.angle>10 || line.angle<-10) continue
                for (element in line.elements) {
                    val text = element.text.trim()
                    Log.i("ImageReader", "Found number: $text")
                    val number = text.toIntOrNull() ?: continue
                    // Typically lotto numbers are 1..90, filter anything absurd
                   // if (number !in 1..90) continue

                    val box: Rect = element.boundingBox ?: continue
                    val center = Point(box.centerX(), box.centerY())

                    results.add(
                        DetectedNumber(
                            value = number,
                            position = GridPosition(1,1),
                            bbox = Rect(box)
                        )
                    )
                }
            }
        }

//        // Deduplicate per cell: keep the largest bbox (most likely the big number) if multiple
//        val byCell = results.groupBy { it.position }
//        val deduped = byCell.values.mapNotNull { candidates ->
//            candidates.maxByOrNull { it.bbox.width().toLong() * it.bbox.height().toLong() }
//        }

        Log.d("ImageReader", "Detected ${results.size} numbers")
        return results
    }

    companion object {
        private val recognizer by lazy { TextRecognition.getClient(DEFAULT_OPTIONS) }
    }

    suspend fun recognize(image: InputImage): Text? = suspendCancellableCoroutine { cont ->
        recognizer
            .process(image)
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener {
                Log.e("ImageReader", "Text recognition failed", it)
                cont.resume(null)
            }
    }

    fun detectAndCropRect(bitmap: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        // Convert to grayscale
        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)

        // Apply threshold
        val thresh = Mat()
        Imgproc.threshold(gray, thresh, 127.0, 255.0, Imgproc.THRESH_BINARY)

        // Find contours
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        // Find largest contour
        val maxContour = contours.maxByOrNull { Imgproc.contourArea(it) } ?: return bitmap

        // Approximate contour to get rectangle
        val peri = Imgproc.arcLength(MatOfPoint2f(*maxContour.toArray()), true)
        val approx = MatOfPoint2f()
        Imgproc.approxPolyDP(MatOfPoint2f(*maxContour.toArray()), approx, 0.02 * peri, true)

        // Get corner points
        val points = approx.toArray()
        if (points.size != 4) return bitmap

        // Apply perspective transform
        val result = Mat()
        val src = MatOfPoint2f(*points)
        val dst = MatOfPoint2f(
            org.opencv.core.Point(0.0, 0.0),
            org.opencv.core.Point(bitmap.width - 1.0, 0.0),
            org.opencv.core.Point(bitmap.width - 1.0, bitmap.height - 1.0),
            org.opencv.core.Point(0.0, bitmap.height - 1.0)
        )
        val transform = Imgproc.getPerspectiveTransform(src, dst)
        Imgproc.warpPerspective(mat, result, transform, mat.size())

        // Convert back to bitmap
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config!!)
        Utils.matToBitmap(result, resultBitmap)
        return resultBitmap
    }

}