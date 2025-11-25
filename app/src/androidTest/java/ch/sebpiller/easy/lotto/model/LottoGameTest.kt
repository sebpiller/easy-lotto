package ch.sebpiller.easy.lotto.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import ch.sebpiller.easy.lotto.processing.ImageReader
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
                ImageReader.DetectedNumber(value = 4, position = ImageReader.GridPosition(0, 0)),
                ImageReader.DetectedNumber(value = 12, position = ImageReader.GridPosition(0, 1)),
                ImageReader.DetectedNumber(value = 46, position = ImageReader.GridPosition(0, 4)),
                ImageReader.DetectedNumber(value = 51, position = ImageReader.GridPosition(0, 5)),
                ImageReader.DetectedNumber(value = 77, position = ImageReader.GridPosition(0, 7)),

                ImageReader.DetectedNumber(value = 10, position = ImageReader.GridPosition(1, 1)),
                ImageReader.DetectedNumber(value = 36, position = ImageReader.GridPosition(1, 3)),
                ImageReader.DetectedNumber(value = 57, position = ImageReader.GridPosition(1, 5)),
                ImageReader.DetectedNumber(value = 68, position = ImageReader.GridPosition(1, 6)),
                ImageReader.DetectedNumber(value = 80, position = ImageReader.GridPosition(1, 8)),

                ImageReader.DetectedNumber(value = 31, position = ImageReader.GridPosition(2, 3)),
                ImageReader.DetectedNumber(value = 48, position = ImageReader.GridPosition(2, 4)),
                ImageReader.DetectedNumber(value = 66, position = ImageReader.GridPosition(2, 6)),
                ImageReader.DetectedNumber(value = 74, position = ImageReader.GridPosition(2, 7)),
                ImageReader.DetectedNumber(value = 90, position = ImageReader.GridPosition(2, 8)),
            )
        )
        grid.checkValidGrid()
        grid.printToConsole()
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

    }
}