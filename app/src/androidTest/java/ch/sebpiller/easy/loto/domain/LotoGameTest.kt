package ch.sebpiller.easy.loto.domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith


@SmallTest
@RunWith(AndroidJUnit4::class)
internal class LotoGameTest {

    @Test
    fun test_game() = runBlocking {
        val game = LotoGame()
        val grid = LotoGrid.fromNumbers(
            listOf(
                LotoNum(4, XY()),
                LotoNum(12, XY(1)),
                LotoNum(46, XY(4)),
                LotoNum(51, XY(5)),
                LotoNum(77, XY(7)),

                LotoNum(10, XY(1, 1)),
                LotoNum(36, XY(3, 1)),
                LotoNum(57, XY(5, 1)),
                LotoNum(68, XY(6, 1)),
                LotoNum(80, XY(8, 1)),

                LotoNum(31, XY(3, 2)),
                LotoNum(48, XY(4, 2)),
                LotoNum(66, XY(6, 2)),
                LotoNum(74, XY(7, 2)),
                LotoNum(90, XY(8, 2)),
            )
        )
        grid.asValidGrid()
        grid.print()
        game.addGrid(grid)
        game.reset();

        game.registerPickedNumber(4)
        game.registerPickedNumber(12)
        game.registerPickedNumber(46)
        game.registerPickedNumber(51)
        game.registerPickedNumber(77)

        Assert.assertTrue("You should have win :(", game.checkWin())

        game.nextPart()
        Assert.assertFalse("You should not have win :(", game.checkWin())

        Assert.assertNull(game.mostWantedNumber())
        game.registerPickedNumber(10)
        game.registerPickedNumber(36)
        game.registerPickedNumber(57)
        Assert.assertNull(game.mostWantedNumber()) // need 2 numbers to win

        game.registerPickedNumber(68)
        Assert.assertEquals(80, game.mostWantedNumber())

        // after all numbers have been added, some checks:
        for (i in 1..90) game.registerPickedNumber(i)
        Assert.assertNull(game.mostWantedNumber()) // no more numbers can be found
        Assert.assertTrue("You should have win :(", game.checkWin())

    }
}