package ch.sebpiller.easy.loto.ui

import ch.sebpiller.easy.loto.domain.LotoGame
import ch.sebpiller.easy.loto.ui.viewmodel.LotoGridViewModel

data class GameUiState(
    val step: LotoGame.GameStep = LotoGame.GameStep.QUINE,
    val grids: List<LotoGridViewModel> = emptyList(),
    val mostWanted: Int? = null,
    val numbers: Set<Int> = emptySet(),
)