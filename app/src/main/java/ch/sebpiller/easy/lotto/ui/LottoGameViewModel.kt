package ch.sebpiller.easy.lotto.ui

import androidx.lifecycle.ViewModel
import ch.sebpiller.easy.lotto.model.Lotto
import ch.sebpiller.easy.lotto.model.LottoGrid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LottoGameViewModel : ViewModel() {
    private val game = Lotto.game

    private val _grids = MutableStateFlow<Set<LottoGridViewModel>>(setOf())

    val grids: StateFlow<Set<LottoGridViewModel>> = _grids.asStateFlow()

    private val _ui = MutableStateFlow(GameUiState())
    val ui: StateFlow<GameUiState> = _ui.asStateFlow()

    init { emit() }

    private fun emit() {
        _ui.value = GameUiState(
            step = game.step,
            numbers = game.numbers.toSet(),
            grids = grids.value.toList(),
            mostWanted = game.mostWantedNumber(),
        )
    }

    // intents (called by UI)
    fun pushNumber(n: Int) {
        game.pushNumber(n)
        emit()
    }

    fun addGrid(grid: LottoGrid) {
        _grids.update {  it+(LottoGridViewModel(grid)) }
        game.addGrid(grid)
        emit()
    }

    fun nextPart() {
        game.nextPart()
        emit()
    }

    fun reset() {
        game.reset()
        emit()
    }
}