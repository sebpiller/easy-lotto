package ch.sebpiller.easy.loto.domain


import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith


@SmallTest
@RunWith(AndroidJUnit4::class)
class LotoGridTest {

    @Test
    fun test_grid() = runBlocking {
        val game = LotoGrid.fromNumbers(
            listOf(
                LotoNum(
                    value = 1, position = XY(),
                )
            )
        ).asValidGrid()

    }
}