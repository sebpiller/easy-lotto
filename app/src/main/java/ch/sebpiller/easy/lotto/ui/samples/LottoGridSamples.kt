package ch.sebpiller.easy.lotto.ui.samples

import ch.sebpiller.easy.lotto.model.LottoGrid
import ch.sebpiller.easy.lotto.model.LottoNumber
import ch.sebpiller.easy.lotto.model.NumberLocation

class LottoGridSamples {
    companion object {
        val SAMPLE1 = LottoGrid.fromNumbers(
            listOf(
                // Row 0
                LottoNumber(1, NumberLocation(0, 0)),
                LottoNumber(12, NumberLocation(0, 1)),
                LottoNumber(26, NumberLocation(0, 2)),
                LottoNumber(40, NumberLocation(0, 4)),
                LottoNumber(90, NumberLocation(0, 8)),
                // Row 1
                LottoNumber(5, NumberLocation(1, 0)),
                LottoNumber(18, NumberLocation(1, 1)),
                LottoNumber(29, NumberLocation(1, 2)),
                LottoNumber(55, NumberLocation(1, 5)),
                LottoNumber(63, NumberLocation(1, 6)),
                // Row 2
                LottoNumber(7, NumberLocation(2, 0)),
                LottoNumber(21, NumberLocation(2, 2)),
                LottoNumber(33, NumberLocation(2, 3)),
                LottoNumber(47, NumberLocation(2, 4)),
                LottoNumber(84, NumberLocation(2, 8)),
            )
        )
    }

}
