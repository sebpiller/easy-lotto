package ch.sebpiller.easy.lotto.domain

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
                LottoNum(4, XY(0, 0)),
                LottoNum(12, XY(0, 1)),
                LottoNum(46, XY(0, 4)),
                LottoNum(51, XY(0, 5)),
                LottoNum(77, XY(0, 7)),

                LottoNum(10, XY(1, 1)),
                LottoNum(36, XY(1, 3)),
                LottoNum(57, XY(1, 5)),
                LottoNum(68, XY(1, 6)),
                LottoNum(80, XY(1, 8)),

                LottoNum(31, XY(2, 3)),
                LottoNum(48, XY(2, 4)),
                LottoNum(66, XY(2, 6)),
                LottoNum(74, XY(2, 7)),
                LottoNum(90, XY(2, 8)),
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