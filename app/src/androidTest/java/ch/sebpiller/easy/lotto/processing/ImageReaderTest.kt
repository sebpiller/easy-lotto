package ch.sebpiller.easy.lotto.processing

import android.graphics.BitmapFactory
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import ch.sebpiller.easy.lotto.model.LottoGrid
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
internal class ImageReaderTest {

    @Test
    fun test_cam_read_lotto_grid_1() = runBlocking {
        val res = recognizeCells("lotto_grid.webp")
        Assert.assertEquals(15, res.size)

        assertFoundAt(res, 7, 1, 0)
        assertFoundAt(res, 8, 2, 0)
        assertFoundAt(res, 16, 0, 1)
        assertFoundAt(res, 16, 0, 1)
        assertFoundAt(res, 25, 1, 2)
        assertFoundAt(res, 28, 2, 2)
        assertFoundAt(res, 35, 0, 3)
        assertFoundAt(res, 39, 2, 3)
        assertFoundAt(res, 42, 1, 4)
        assertFoundAt(res, 51, 1, 5)
        assertFoundAt(res, 54, 0, 5)
        assertFoundAt(res, 66, 2, 6)
        assertFoundAt(res, 74, 1, 7)
        assertFoundAt(res, 76, 0, 7)
        assertFoundAt(res, 83, 2, 8)
        assertFoundAt(res, 89, 0, 8)

        val grid = LottoGrid.fromNumbers(res)
        grid.printToConsole()
        Assert.assertTrue("Grid invalid", grid.checkValidGrid())

        return@runBlocking
    }

    @Test
    fun test_cam_read_lotto_grid_2() = runBlocking {
        val res = recognizeCells("lotto_grid2.jpg")
        Assert.assertEquals(15, res.size)

        assertFoundAt(res, 5, 1, 0)
        assertFoundAt(res, 12, 2, 1)
        assertFoundAt(res, 19, 0, 1)
        assertFoundAt(res, 22, 0, 2)
        assertFoundAt(res, 24, 1, 2)
        assertFoundAt(res, 30, 2, 3)
        assertFoundAt(res, 38, 1, 3)
        assertFoundAt(res, 45, 0, 4)
        assertFoundAt(res, 49, 2, 4)
        assertFoundAt(res, 51, 1, 5)
        assertFoundAt(res, 63, 0, 6)
        assertFoundAt(res, 66, 2, 6)
        assertFoundAt(res, 71, 1, 7)
        assertFoundAt(res, 77, 0, 7)
        assertFoundAt(res, 89, 2, 8)

        val grid = LottoGrid.fromNumbers(res)
        grid.printToConsole()
        Assert.assertTrue("Grid invalid", grid.checkValidGrid())

        return@runBlocking
    }

    @Test
    fun test_cam_read_lotto_grid_3() = runBlocking {
        val res = recognizeCells("loto-1174877_960_720.png")

        // TODO
        //Assert.assertSame(15, res.size)

        //assertFoundAt(res, 5, 1, 0)

        val grid = LottoGrid.fromNumbers(res)
        grid.printToConsole()
        Assert.assertTrue("Grid invalid", grid.checkValidGrid())

        return@runBlocking
    }

    private fun assertFoundAt(
        res: List<ImageReader.DetectedNumber>,
        v: Int,
        r: Int,
        c: Int
    ) {
        for (i in res) {
            if (i.value == v && i.position.row == r && i.position.col == c) {
                // TEST OK
                return
            }
        }

        Assert.fail("could not find a number $v at position [$r,$c]")
    }


    private suspend fun recognizeCells(img: String): List<ImageReader.DetectedNumber> {
        val start = System.currentTimeMillis()
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageStream = context.assets.open(img)

        val originalBitmap = BitmapFactory.decodeStream(imageStream)
        val coll = ImageReader().read(InputImage.fromBitmap(originalBitmap, 0))

        Log.i("ImageReaderTest", "Read duration ${System.currentTimeMillis() - start}ms")

        return List(coll.size) { i -> coll[i] }
    }
}