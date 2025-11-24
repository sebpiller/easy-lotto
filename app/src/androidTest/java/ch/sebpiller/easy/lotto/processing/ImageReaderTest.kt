package ch.sebpiller.easy.lotto.processing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@SmallTest
@RunWith(AndroidJUnit4::class)
internal class ImageReaderTest {

    private fun saveBitmapToFile(bitmap: Bitmap, filename: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.getExternalFilesDir(null), filename)
        println(file.absolutePath)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    @Test
    fun testRead_withAssetImage_detectsNumbersCorrectly() = runBlocking {
        val start = System.currentTimeMillis()
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageStream = context.assets.open("lotto_grid.png")


        val r = ImageReader()

        val bitmap = r.detectAndCropRect(BitmapFactory.decodeStream(imageStream))

        val subImages = mutableListOf<Bitmap>()
        val rows = 3
        val cols = 9
        val cellWidth = bitmap!!.width / cols
        val cellHeight = bitmap.height / rows

        val coll = mutableListOf<ImageReader.DetectedNumber>()




        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val subImage = Bitmap.createBitmap(
                    bitmap,
                    col * cellWidth,
                    row * cellHeight,
                    cellWidth,
                    cellHeight
                )
                subImages.add(subImage)
                saveBitmapToFile(subImage, "subimage_${row}_${col}.png")
            }
        }



//        subImages.forEach {
//            val x = r.read(InputImage.fromBitmap(it, 0))
//            if (!x.isEmpty())
//                coll.add(x.first())
//        }



        coroutineScope {
            val deferreds = subImages.map { bitmap ->
                async {
                    val result = r.read(InputImage.fromBitmap(bitmap, 0))
                    result.ifEmpty { null }
                }
            }

            coll.addAll(deferreds.awaitAll().filterNotNull().flatten())
        }

        Log.i("ImageReaderTest", "Read ${subImages.size} images in ${System.currentTimeMillis() - start}ms")
        Log.i("ImageReaderTest", "Found " + coll.size + ": " + coll.toString())
        coll.sortBy { it.value }
        coll.forEach { println(it) }

        return@runBlocking
    }
}