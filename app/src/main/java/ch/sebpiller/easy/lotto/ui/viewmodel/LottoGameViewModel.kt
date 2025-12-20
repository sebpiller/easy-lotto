package ch.sebpiller.easy.lotto.ui.viewmodel

import androidx.lifecycle.ViewModel
import ch.sebpiller.easy.lotto.model.Lotto
import ch.sebpiller.easy.lotto.model.LottoGrid
import ch.sebpiller.easy.lotto.ui.GameUiState
import ch.sebpiller.easy.lotto.ui.viewmodel.LottoGridViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LottoGameViewModel : ViewModel() {
    private val game = Lotto.Companion.game

    private val _grids = MutableStateFlow<Set<LottoGridViewModel>>(setOf())

    val grids: StateFlow<Set<LottoGridViewModel>> = _grids.asStateFlow()

    private val _ui = MutableStateFlow(GameUiState())
    val ui: StateFlow<GameUiState> = _ui.asStateFlow()

    init {
        emit()
    }

    private fun emit() {
        _ui.value = GameUiState(
            step = game.step,
            numbers = game.pickedNumbers.toSet(),
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
        game.addGrid(grid)
        gridSync()
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

    fun removeAllGrids() {
        game.removeAllGrids()
        gridSync()
        emit()
    }

    private fun gridSync() {
        _grids.update { game.grids.map { LottoGridViewModel(it) }.toSet() }
    }

    fun reloadGrids() {
        gridSync()
        emit()
    }
}