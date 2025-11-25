package ch.sebpiller.easy.lotto.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Rect
import android.util.Log
import androidx.compose.ui.graphics.Paint
import androidx.core.graphics.createBitmap
import com.google.mlkit.common.sdkinternal.MlKitThreadPool
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

class ImageReader {
    private val context: Context

    constructor(context: Context) {
        this.context = context
    }


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


    private fun preprocessImage(bitmap: Bitmap): Bitmap {

        val x = resizeToMax(bitmap, 480, 320)

        val width = x.width
        val height = x.height
        val processedBitmap = createBitmap(width, height)
        val canvas = Canvas(processedBitmap)
        val paint = android.graphics.Paint()

// Convert to grayscale
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)


// Increase contrast
        colorMatrix.set(
            floatArrayOf(
                2f, 0f, 0f, 0f, -128f,
                0f, 2f, 0f, 0f, -128f,
                0f, 0f, 2f, 0f, -128f,
                0f, 0f, 0f, 1f, 0f
            )
        )

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(x, 0f, 0f, paint)


        return DarkContentExtractor(2).extract(processedBitmap)

    }


    private fun saveBitmapToFile(bitmap: Bitmap, filename: String) {
        val file = File(context.getExternalFilesDir(null), filename)
        println(file.absolutePath)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    /**
     * Resize the given [bitmap] so that it fits within [maxWidth] x [maxHeight],
     * preserving the original aspect ratio. If the bitmap is already within the
     * target bounds, the original instance is returned without modification.
     *
     * Defaults to a maximum of 480 x 320 as requested.
     */
    fun resizeToMax(bitmap: Bitmap, maxWidth: Int = 480, maxHeight: Int = 320): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Nothing to do if already small enough
        if (width <= maxWidth && height <= maxHeight) return bitmap

        // Compute a uniform scale to fit within the bounds while preserving aspect ratio
        val scale = minOf(
            maxWidth.toFloat() / width.toFloat(),
            maxHeight.toFloat() / height.toFloat()
        )

        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    suspend fun read(xxxx: InputImage): List<DetectedNumber> {
        val processedBitmap = preprocessImage(xxxx.bitmapInternal!!)

        val cellHeight = processedBitmap.height / 3f
        val cellWidth = processedBitmap.width / 9f

        val bmp1 = createBitmap(processedBitmap.width, processedBitmap.height, Bitmap.Config.ARGB_8888)
        val bmp2 = createBitmap(processedBitmap.width, processedBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas1 = Canvas(bmp1)
        val canvas2 = Canvas(bmp2)

        val paint = android.graphics.Paint()
        paint.setColor(Color.WHITE)
        paint.style = android.graphics.Paint.Style.FILL

        canvas1.drawRect(canvas1.clipBounds, paint)
        canvas2.drawRect(canvas2.clipBounds, paint)

        for (x in intArrayOf(0, 2, 4, 6, 8)) {
            val rect = Rect((x * cellWidth).toInt(), 0, ((x + 1) * cellWidth).toInt(), (cellHeight * 3f).toInt())
            canvas1.drawBitmap(processedBitmap, rect, rect, null)
        }

        for (x in intArrayOf(1, 3, 5, 7)) {
            val rect = Rect((x * cellWidth).toInt(), 0, ((x + 1) * cellWidth).toInt(), (cellHeight * 3f).toInt())
            canvas2.drawBitmap(processedBitmap, rect, rect, null)
        }

        saveBitmapToFile(bmp1, "bmp1.png")
        saveBitmapToFile(bmp2, "bmp2.png")


        val results = mutableListOf<DetectedNumber>()

        val xxx = processedBitmap
        //for (xxx in arrayOf(bmp1, bmp2))
        if (true) {

            val visionText = recognize(InputImage.fromBitmap(xxx, 0)) ?: return emptyList()

            for (block in visionText.textBlocks) {
                for (line in block.lines) {
                    if (line.angle > 10 || line.angle < -10) continue
                    for (element in line.elements) {
                        val text = element.text.trim()
                        Log.i("ImageReader", "Found text: $text")

                       // if (element.confidence < 0.5) continue
                        // if(element.boundingBox!!.width() < (image.width*0.5)) continue
                        val bb = element.boundingBox!!
                        val minHeight = xxx.height * 0.333 * 0.25 // each number must be at least 25% height of a cell
                        if (bb.height() < minHeight) continue

                        val row = ((bb.top / cellHeight).toInt())
                        val from = (((bb.left + 10) / cellWidth).toInt())

                        // ok we found something that looks like a valid box. Tokenize it according to cells
                        if (bb.width() > (xxx.width / 9f)) {
                            val to = ((bb.left + bb.width() - 10) / cellWidth).toInt()

                            Log.i("ImageReader", "!!! Found number in cell $row, $from-$to")

                            for (i in from..to) {
                                val subImage = Bitmap.createBitmap(
                                    xxx,
                                    (i * cellWidth).toInt(),
                                    (row * cellHeight).toInt(),
                                    cellWidth.toInt(),
                                    cellHeight.toInt()
                                )


                                val x = recognize(InputImage.fromBitmap(subImage, 0))

                                val text = x!!.textBlocks.map { block ->
                                    block
                                        .lines
                                        .filter { line -> line.angle < 10 && line.angle > -10 }
                                        .filter { line -> line.boundingBox!!.height() > minHeight }
                                        .map { line -> line.text }
                                }.flatten().joinToString(" ")
                                if (text.isBlank()) continue

                                val _x = text.toIntOrNull()

                                if (_x != null) {
                                    results.add(DetectedNumber(_x, GridPosition(row = row, col = i), bb))
                                }
                            }
                        } else {

                            val _x = text.toIntOrNull()

                            if (_x != null)

                                results.add(
                                    DetectedNumber(
                                        value = _x,
                                        position = GridPosition(row, from),
                                        bbox = element.boundingBox!!
                                    )
                                )
                        }
                    }
                }
            }
        }

        // Deduplicate per cell: keep the largest bbox (most likely the big number) if multiple
        val byCell = results.groupBy { it.position }
        val deduped = byCell.values.mapNotNull { candidates ->
            candidates.maxByOrNull { it.bbox.width().toLong() * it.bbox.height().toLong() }
        }

        Log.d("ImageReader", "Detected ${results.size} numbers")
        return deduped
    }

    companion object {
        private val recognizer by lazy {
            TextRecognition.getClient(
                TextRecognizerOptions.Builder()
                    .setExecutor(MlKitThreadPool())
                    .build()
            )
        }
    }

    var i = 0
    suspend fun recognize(image: InputImage): Text? = suspendCancellableCoroutine { cont ->
        i++
        recognizer
            .process(image)
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener {
                Log.e("ImageReader", "Text recognition failed", it)
                cont.resume(null)
            }
        Log.i("ImageReader", "!!!! Image recognition success: $i")
    }
//
//    fun detectAndCropRect(bitmap: Bitmap): Bitmap {
//        val mat = Mat()
//        Utils.bitmapToMat(bitmap, mat)
//
//        // Convert to grayscale
//        val gray = Mat()
//        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
//
//        // Apply threshold
//        val thresh = Mat()
//        Imgproc.threshold(gray, thresh, 127.0, 255.0, Imgproc.THRESH_BINARY)
//
//        // Find contours
//        val contours = ArrayList<MatOfPoint>()
//        val hierarchy = Mat()
//        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
//
//        // Find largest contour
//        val maxContour = contours.maxByOrNull { Imgproc.contourArea(it) } ?: return bitmap
//
//        // Approximate contour to get rectangle
//        val peri = Imgproc.arcLength(MatOfPoint2f(*maxContour.toArray()), true)
//        val approx = MatOfPoint2f()
//        Imgproc.approxPolyDP(MatOfPoint2f(*maxContour.toArray()), approx, 0.02 * peri, true)
//
//        // Get corner points
//        val points = approx.toArray()
//        if (points.size != 4) return bitmap
//
//        // Apply perspective transform
//        val result = Mat()
//        val src = MatOfPoint2f(*points)
//        val dst = MatOfPoint2f(
//            org.opencv.core.Point(0.0, 0.0),
//            org.opencv.core.Point(bitmap.width - 1.0, 0.0),
//            org.opencv.core.Point(bitmap.width - 1.0, bitmap.height - 1.0),
//            org.opencv.core.Point(0.0, bitmap.height - 1.0)
//        )
//        val transform = Imgproc.getPerspectiveTransform(src, dst)
//        Imgproc.warpPerspective(mat, result, transform, mat.size())
//
//        // Convert back to bitmap
//        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config!!)
//        Utils.matToBitmap(result, resultBitmap)
//        return resultBitmap
//    }

}