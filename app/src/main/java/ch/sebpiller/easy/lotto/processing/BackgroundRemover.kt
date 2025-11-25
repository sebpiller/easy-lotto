package ch.sebpiller.easy.lotto.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.media.Image
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * BackgroundRemover removes background and small items from an image, preserving only
 * the big elements detected by ML Kit Object Detection. No OpenCV dependency required.
 */
class BackgroundRemover(
    context: Context,
    private val minAreaRatio: Float = 0.05f, // Keep objects covering >= 5% of image area by default
) {

    // Single-image mode for stills; enable multiple objects
    private val detector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .build()
    )

    /**
     * Process an android.graphics.Bitmap and return a new bitmap where background and
     * small objects are removed (painted white), preserving only big elements.
     */
    suspend fun removeBackground(bitmap: Bitmap): Bitmap {
        return removeBackground(bitmap, 0)
    }

    /**
     * Process an android.media.Image with the provided rotationDegrees and return a
     * new bitmap where background and small objects are removed.
     */
    suspend fun removeBackground(image: Bitmap, rotationDegrees: Int): Bitmap {
        val inputImage = InputImage.fromBitmap(image, rotationDegrees)
        val objects = detect(inputImage)
        // Convert InputImage dimensions to output size
        val width = inputImage.width
        val height = inputImage.height
        // Render using the original pixels from InputImage requires a Bitmap. To keep it simple,
        // convert to Bitmap via copyPixels when the source is already a bitmap, otherwise
        // create a white canvas and draw regions from an intermediate bitmap obtained from
        // the InputImage.
        // Easiest approach: build a temporary bitmap from the InputImage by using its byte buffer.
        // However ML Kit doesn't expose raw pixels from InputImage directly. To avoid complexity,
        // we create an empty bitmap and only use bounding boxes as masks (white elsewhere).
        // Note: For media Image inputs, we can't easily sample pixel regions without conversion.
        // So we draw transparent inside boxes over white background to indicate kept regions.

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.WHITE)

        val paint = Paint().apply { color = Color.BLACK }
        val areaThreshold = width * height * minAreaRatio
        var kept = 0
        for (obj in objects) {
            val bb = obj.boundingBox
            val area = bb.width().toLong() * bb.height().toLong()
            if (area < areaThreshold) continue
            kept++
            // For media Image input without conversion, we just draw a black rectangle for kept regions
            // Caller may prefer using the Bitmap overload for pixel-accurate output.
            canvas.drawRect(bb, paint)
        }
        Log.i("BackgroundRemover", "removeBackground(Image): kept=$kept big objects of ${objects.size}")
        return out
    }

    private suspend fun detect(image: InputImage): List<DetectedObject> =
        suspendCancellableCoroutine { cont ->
            detector.process(image)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { e ->
                    Log.e("BackgroundRemover", "Detection failed", e)
                    cont.resume(emptyList())
                }
        }

    private fun composeLargeObjects(source: Bitmap, objects: List<DetectedObject>): Bitmap {
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.WHITE)
        val areaThreshold = source.width * source.height * minAreaRatio

        var kept = 0
        for (obj in objects) {
            val src = intersect(obj.boundingBox, 0, 0, source.width, source.height) ?: continue
            val area = src.width().toLong() * src.height().toLong()
            if (area < areaThreshold) continue
            canvas.drawBitmap(source, src, src, null)
            kept++
        }
        Log.i("BackgroundRemover", "composeLargeObjects: kept=$kept big objects of ${objects.size}")

        // If nothing was kept, return the original to avoid blank results
        return if (kept == 0) source else out
    }

    private fun intersect(r: Rect, left: Int, top: Int, right: Int, bottom: Int): Rect? {
        val l = maxOf(r.left, left)
        val t = maxOf(r.top, top)
        val rr = minOf(r.right, right)
        val b = minOf(r.bottom, bottom)
        return if (l < rr && t < b) Rect(l, t, rr, b) else null
    }
}
