package ch.sebpiller.easy.lotto.ui.samples

import ch.sebpiller.easy.lotto.domain.LottoGrid
import ch.sebpiller.easy.lotto.domain.LottoNum
import ch.sebpiller.easy.lotto.domain.XY

class LottoGridSamples {
    companion object {
        val SAMPLE1 = LottoGrid.fromNumbers(
            listOf(
                // Row 0
                LottoNum(1, XY()),
                LottoNum(12, XY(1)),
                LottoNum(26, XY(2)),
                LottoNum(40, XY(4)),
                LottoNum(90, XY(8)),
                // Row 1
                LottoNum(5, XY(row = 1)),
                LottoNum(18, XY(1, 1)),
                LottoNum(29, XY(2, 1)),
                LottoNum(55, XY(5, 1)),
                LottoNum(63, XY(6, 1)),
                // Row 2
                LottoNum(7, XY(row = 2)),
                LottoNum(21, XY(2, 2)),
                LottoNum(33, XY(3, 2)),
                LottoNum(47, XY(4, 2)),
                LottoNum(84, XY(8, 2)),
            )
        )
    }

}
