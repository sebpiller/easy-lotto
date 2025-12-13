package ch.sebpiller.easy.lotto.model


import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith


@SmallTest
@RunWith(AndroidJUnit4::class)
class LottoGridTest {

    @Test
    fun test_grid() = runBlocking {
        val game = LottoGrid.fromNumbers(
            listOf(
                LottoNumber(
                    value = 1, position = NumberLocation(0, 0),
                )
            )
        ).asValidGrid()

    }
}