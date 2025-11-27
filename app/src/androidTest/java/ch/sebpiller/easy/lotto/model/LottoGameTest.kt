package ch.sebpiller.easy.lotto.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith


@SmallTest
@RunWith(AndroidJUnit4::class)
internal class LottoGameTest {

    @Test
    fun test_game() = runBlocking {
        val game = LottoGame()
        val grid = LottoGrid.fromNumbers(
            listOf(
                LottoNumber(4, NumberLocation(0, 0)),
                LottoNumber(12, NumberLocation(0, 1)),
                LottoNumber(46, NumberLocation(0, 4)),
                LottoNumber(51, NumberLocation(0, 5)),
                LottoNumber(77, NumberLocation(0, 7)),

                LottoNumber(10, NumberLocation(1, 1)),
                LottoNumber(36, NumberLocation(1, 3)),
                LottoNumber(57, NumberLocation(1, 5)),
                LottoNumber(68, NumberLocation(1, 6)),
                LottoNumber(80, NumberLocation(1, 8)),

                LottoNumber(31, NumberLocation(2, 3)),
                LottoNumber(48, NumberLocation(2, 4)),
                LottoNumber(66, NumberLocation(2, 6)),
                LottoNumber(74, NumberLocation(2, 7)),
                LottoNumber(90, NumberLocation(2, 8)),
            )
        )
        grid.asValidGrid()
        grid.print()
        game.addGrid(grid)
        game.reset();

        game.pushNumber(4)
        game.pushNumber(12)
        game.pushNumber(46)
        game.pushNumber(51)
        game.pushNumber(77)

        Assert.assertTrue("You should have win :(", game.checkWin())

        game.nextPart()
        Assert.assertFalse("You should not have win :(", game.checkWin())

        Assert.assertNull(game.mostWantedNumber())
        game.pushNumber(10)
        game.pushNumber(36)
        game.pushNumber(57)
        Assert.assertNull(game.mostWantedNumber()) // need 2 numbers to win

        game.pushNumber(68)
        Assert.assertEquals(80, game.mostWantedNumber())

        // after all numbers have been added, some checks:
        for (i in 1..90) game.pushNumber(i)
        Assert.assertNull(game.mostWantedNumber()) // no more numbers can be found
        Assert.assertTrue("You should have win :(", game.checkWin())

    }
}