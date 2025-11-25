package ch.sebpiller.easy.lotto.processing

import android.graphics.Bitmap
import android.graphics.Color

/**
 * DarkContentExtractor processes a bitmap as follows:
 * 1) blur (box blur, separable, CPU-only, no extra deps)
 * 2) convert to grayscale
 * 3) keep only the darkest content (by percentile threshold), paint others white
 *
 * The goal is to highlight dark foreground content (e.g., ink on paper) and remove background.
 */
class DarkContentExtractor(
    /**
     * Blur radius in pixels for a simple box blur. Use 0 to skip blur. Typical: 2..5.
     */
    private val blurRadius: Int = 3,

    /**
     * Fraction of (blurred) grayscale pixels to preserve as the darkest content.
     * Example: 0.15f keeps the darkest 15%.
     */
    private val keepDarkestPercent: Float = 0.10f
) {

    init {
        require(keepDarkestPercent in 0f..1f) { "keepDarkestPercent must be between 0 and 1" }
        require(blurRadius >= 0) { "blurRadius must be >= 0" }
    }

    /**
     * Process the input bitmap and return a new bitmap with white background where
     * only the darkest content is kept (as black), according to [keepDarkestPercent].
     */
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

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val outPixels = IntArray(width * height)
        for (i in blurred.indices) {
            val keep = blurred[i] <= threshold
            // Paint kept content black, others white
            outPixels[i] = if (keep) Color.BLACK else Color.WHITE
        }
        out.setPixels(outPixels, 0, width, 0, 0, width, height)
        return out
    }

    /**
     * Compute a percentile threshold (0..255) from grayscale values.
     * keepPercent = 0.15 => returns intensity so that ~15% values are <= threshold.
     */
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

    /**
     * Fast separable box blur. Performs horizontal then vertical passes.
     * Edges are clamped.
     */
    private fun boxBlur(src: IntArray, width: Int, height: Int, radius: Int): IntArray {
        val tmp = IntArray(src.size)
        val dst = IntArray(src.size)
        // Horizontal pass
        val window = 2 * radius + 1
        for (y in 0 until height) {
            var sum = 0
            val rowStart = y * width
            // Initialize with left edge clamped
            val first = src[rowStart]
            val last = src[rowStart + width - 1]
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
}
