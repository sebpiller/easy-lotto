package ch.sebpiller.easy.lotto.ui

import ch.sebpiller.easy.lotto.model.LottoGame

data class GameUiState(
    val step: LottoGame.GameStep = LottoGame.GameStep.QUINE,
    val grids: List<LottoGridViewModel> = emptyList(),
    val mostWanted: Int? = null,
    val numbers: Set<Int> = emptySet(),
)