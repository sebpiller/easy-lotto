package ch.sebpiller.easy.loto.ui.samples

import ch.sebpiller.easy.loto.domain.LotoGrid
import ch.sebpiller.easy.loto.domain.LotoNum
import ch.sebpiller.easy.loto.domain.XY

class LotoGridSamples {
    companion object {
        val SAMPLE1 = LotoGrid.fromNumbers(
            listOf(
                // Row 0
                LotoNum(1, XY()),
                LotoNum(12, XY(1)),
                LotoNum(26, XY(2)),
                LotoNum(40, XY(4)),
                LotoNum(90, XY(8)),
                // Row 1
                LotoNum(5, XY(row = 1)),
                LotoNum(18, XY(1, 1)),
                LotoNum(29, XY(2, 1)),
                LotoNum(55, XY(5, 1)),
                LotoNum(63, XY(6, 1)),
                // Row 2
                LotoNum(7, XY(row = 2)),
                LotoNum(21, XY(2, 2)),
                LotoNum(33, XY(3, 2)),
                LotoNum(47, XY(4, 2)),
                LotoNum(84, XY(8, 2)),
            )
        )
    }

}
