package ch.sebpiller.easy.lotto.ocr

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

class LottoGridRecognizer {
    private val rows = 3
    private val cols = 9
    private val angleThreshold: Int = 10
    private val confidenceThreshold: Float = 0.5f

    private data class LNPos(
        val value: Int, val pos: NumberLocation, val bounds: Rect
    )

    suspend fun extractAllNumbers(input: InputImage): List<LottoNumber> {
        val processedBitmap = ImagePreparator().preprocessImage(input.bitmapInternal!!)
        val cellHeight = 1f * processedBitmap.height / rows
        val cellWidth = 1f * processedBitmap.width / cols
        val results = mutableListOf<LNPos>()
        val recognized = recognize(InputImage.fromBitmap(processedBitmap, 0)) ?: return listOf<LottoNumber>()

        for (textBlock in recognized.textBlocks) {
            for (line in textBlock.lines
                .filter { it.angle > -angleThreshold && it.angle < angleThreshold }
            ) {
                for (element in line.elements
                    .filter { it.confidence >= confidenceThreshold }
                    .filter { it.boundingBox!!.height() > 0.25 * (processedBitmap.height / rows) }
                ) {
                    Log.d("LottoGridRecognizer", "Found raw text: '${element.text}' with enough confidence")

                    val text = element.text.trim().filter { it.isDigit() }
                    val bb = element.boundingBox!!

                    val hitRow = (bb.top / cellHeight).toInt()
                    val hitColFrom = ((bb.left + 10) / cellWidth).toInt()

                    if (bb.width() <= processedBitmap.width / cols) {
                        // the recognized area is not bigger than expected on a single cell
                        // -> this should hold a single number
                        results.add(
                            LNPos(
                                text.toInt(),
                                NumberLocation(hitRow, hitColFrom),
                                bb
                            )
                        )
                    } else {
                        // the recognized area is covering multiple cell - need to tokenize it and recognize
                        // multiple numbers

                        // add an extra space if the recognized text starts at column 0
                        // (the only column with single digit numbers)
                        val tokens = if (hitColFrom == 0) " $text" else text

                        // at this point we should have an even number of chars
                        assert(tokens.length % 2 == 0) { "should have an even number of chars: '$tokens'" }

                        for (i in 0..<tokens.length step 2) {
                            val token = tokens.substring(i, i + 2).trim().toInt()
                            results.add(
                                LNPos(
                                    token,
                                    NumberLocation(hitRow, (token / 10).coerceAtMost(8)),
                                    bb // FIXME create bounds for expected location
                                )
                            )
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
                    .build()
            )
        }
    }

    private suspend fun recognize(image: InputImage): Text? = suspendCancellableCoroutine { cont ->
        recognizer
            .process(image)
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener {
                Log.e("LottoGridRecognizer", "Text recognition failed", it)
                cont.resume(null)
            }
    }
}