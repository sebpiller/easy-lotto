package ch.sebpiller.easy.lotto.ocr

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import ch.sebpiller.easy.lotto.model.LottoNumber
import ch.sebpiller.easy.lotto.model.NumberLocation
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LottoGridRecognizer() {

    private var passesNeededForLastRecognition = 0

    private data class LNPos(
        val value: Int, val pos: NumberLocation, val bounds: Rect
    )

    suspend fun extractAllNumbers(input: InputImage): List<LottoNumber> {
        passesNeededForLastRecognition = 0
        val processedBitmap = ImagePreparator().preprocessImage(input.bitmapInternal!!)
        val cellHeight = processedBitmap.height / 3f
        val cellWidth = processedBitmap.width / 9f
        val results = mutableListOf<LNPos>()
        val recognized = recognize(InputImage.fromBitmap(processedBitmap, 0)) ?: return listOf<LottoNumber>()

        for (block in recognized.textBlocks) {
            for (line in block.lines) {
                if (line.angle > 10 || line.angle < -10) continue

                for (element in line.elements) {
                    val text = element.text.trim()
                    //Log.d("ImageReader", "Found raw text: $text")

                    if (element.confidence < 0.5) continue
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
                                LNPos(num, NumberLocation(row, from), bb)
                            )
                        }
                    } else {
                        val to = ((bb.left + bb.width() - 10) / cellWidth).toInt()
                        //Log.i("ImageReader", "Found text overlapping several cells at $row, $from-$to")

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
                                block.lines
                                    .filter { line -> line.angle < 10 && line.angle > -10 }
                                    .filter { line -> line.boundingBox!!.height() > minHeight }
                                    .map { line -> line.text }
                            }.flatten().joinToString(" ")
                            if (text.isBlank()) continue

                            val num = text.toIntOrNull()
                            if (num != null) {
                                results.add(LNPos(num, NumberLocation(row = row, col = i), bb))
                            }
                        }
                    }
                }
            }
        }

        // Deduplicate per cell: keep the largest bbox (most likely the big number) if multiple
        return results
            .groupBy { it.pos }
            .values
            .mapNotNull { x -> x.maxByOrNull { it.bounds.width() * it.bounds.height() } }
            .sortedBy { it.value }
            .map { LottoNumber(it.value, it.pos) }
            .toList()
    }

    companion object {
        private val recognizer by lazy {
            TextRecognition.getClient(
                TextRecognizerOptions.Builder()
                    //.setExecutor(MlKitThreadPool())
                    .build()
            )
        }
    }

    private suspend fun recognize(image: InputImage): Text? = suspendCancellableCoroutine { cont ->
        passesNeededForLastRecognition++
        recognizer
            .process(image)
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener {
                Log.e("LottoGridRecognizer", "Text recognition failed", it)
                cont.resume(null)
            }
        Log.i("LottoGridRecognizer", "pass $passesNeededForLastRecognition done")
    }
}