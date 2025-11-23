package ch.sebpiller.easy.lotto.processing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ImageReaderTest {

    @Test
    fun testRead_withAssetImage_detectsNumbersCorrectly() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageStream = context.assets.open("lotto_grid.bmp")
        

        val bitmap = BitmapFactory.decodeStream(imageStream)

        val subImages = mutableListOf<Bitmap>()
        val rows = 3
        val cols = 9
        val cellWidth = bitmap!!.width / cols
        val cellHeight = bitmap.height / rows

        val r = ImageReader()
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

                val x = r.read(InputImage.fromBitmap(subImage, 0))
                x.forEach { println(it) }
            }
        }


    }
}