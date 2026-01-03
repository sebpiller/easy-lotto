package ch.sebpiller.easy.lotto.ui.samples

import ch.sebpiller.easy.lotto.domain.LottoGrid
import ch.sebpiller.easy.lotto.domain.LottoNum
import ch.sebpiller.easy.lotto.domain.XY

class LottoGridSamples {
    companion object {
        val SAMPLE1 = LottoGrid.fromNumbers(
            listOf(
                // Row 0
                LottoNum(1, XY(0, 0)),
                LottoNum(12, XY(0, 1)),
                LottoNum(26, XY(0, 2)),
                LottoNum(40, XY(0, 4)),
                LottoNum(90, XY(0, 8)),
                // Row 1
                LottoNum(5, XY(1, 0)),
                LottoNum(18, XY(1, 1)),
                LottoNum(29, XY(1, 2)),
                LottoNum(55, XY(1, 5)),
                LottoNum(63, XY(1, 6)),
                // Row 2
                LottoNum(7, XY(2, 0)),
                LottoNum(21, XY(2, 2)),
                LottoNum(33, XY(2, 3)),
                LottoNum(47, XY(2, 4)),
                LottoNum(84, XY(2, 8)),
            )
        )
    }

}
