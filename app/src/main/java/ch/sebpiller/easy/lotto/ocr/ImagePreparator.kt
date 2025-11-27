package ch.sebpiller.easy.lotto.ocr

import android.graphics.*
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

class ImagePreparator(
    private val blurRadius: Int = 2,
    private val keepDarkestPercent: Float = 0.10f
) {

    init {
        require(keepDarkestPercent in 0f..1f) { "keepDarkestPercent must be between 0 and 1" }
        require(blurRadius >= 0) { "blurRadius must be >= 0" }
    }

    fun extract(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // 1) Read source pixels and convert to grayscale (0..255)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val gray = IntArray(width * height)
        for (i in pixels.indices) {
            val c = pixels[i]
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            // Luma approximation (sRGB): 0.299R + 0.587G + 0.114B
            val y = (0.299f * r + 0.587f * g + 0.114f * b).toInt()
            gray[i] = y.coerceIn(0, 255)
        }

        // 2) Blur grayscale if requested
        val blurred = if (blurRadius > 0) boxBlur(gray, width, height, blurRadius) else gray

        // 3) Compute threshold by percentile and keep only darkest content
        val threshold = percentileThreshold(blurred, keepDarkestPercent)

        val out = createBitmap(width, height)
        val outPixels = IntArray(width * height)
        for (i in blurred.indices) {
            val keep = blurred[i] <= threshold
            // Paint kept content black, others white
            outPixels[i] = if (keep) Color.BLACK else Color.WHITE
        }
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    private fun percentileThreshold(values: IntArray, keepPercent: Float): Int {
        if (values.isEmpty()) return 0
        val hist = IntArray(256)
        for (v in values) hist[v.coerceIn(0, 255)]++
        val total = values.size
        val target = (total * keepPercent).toLong().coerceAtLeast(1)
        var cum = 0L
        for (i in 0..255) {
            cum += hist[i]
            if (cum >= target) return i
        }
        return 255
    }

    private fun boxBlur(src: IntArray, width: Int, height: Int, radius: Int): IntArray {
        val tmp = IntArray(src.size)
        val dst = IntArray(src.size)
        // Horizontal pass
        val window = 2 * radius + 1
        for (y in 0 until height) {
            var sum = 0
            val rowStart = y * width

            // sum of initial window
            for (x in -radius..radius) {
                val xx = when {
                    x < 0 -> 0
                    x >= width -> width - 1
                    else -> x
                }
                sum += src[rowStart + xx]
            }
            tmp[rowStart] = sum / window
            for (x in 1 until width) {
                val addIndex = (x + radius).coerceAtMost(width - 1)
                val removeIndex = (x - radius - 1).coerceAtLeast(0)
                sum += src[rowStart + addIndex] - src[rowStart + removeIndex]
                tmp[rowStart + x] = sum / window
            }
        }

        // Vertical pass
        for (x in 0 until width) {
            var sum = 0
            // sum initial window
            for (y in -radius..radius) {
                val yy = when {
                    y < 0 -> 0
                    y >= height -> height - 1
                    else -> y
                }
                sum += tmp[yy * width + x]
            }
            dst[x] = sum / window
            for (y in 1 until height) {
                val addIndex = (y + radius).coerceAtMost(height - 1)
                val removeIndex = (y - radius - 1).coerceAtLeast(0)
                sum += tmp[addIndex * width + x] - tmp[removeIndex * width + x]
                dst[y * width + x] = sum / window
            }
        }
        return dst
    }


    fun preprocessImage(bitmap: Bitmap): Bitmap {
        val resized = resizeToAtMost(bitmap, 480, 320)

        val width = resized.width
        val height = resized.height
        val processedBitmap = createBitmap(width, height)
        val canvas = Canvas(processedBitmap)
        val paint = Paint()

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

        return extract(processedBitmap)
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

}
