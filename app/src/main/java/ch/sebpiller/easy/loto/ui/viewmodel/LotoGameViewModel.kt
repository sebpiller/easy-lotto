package ch.sebpiller.easy.loto.ui.viewmodel

import androidx.lifecycle.ViewModel
import ch.sebpiller.easy.loto.domain.Loto
import ch.sebpiller.easy.loto.domain.LotoGrid
import ch.sebpiller.easy.loto.ui.GameUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LotoGameViewModel : ViewModel() {
    val game = Loto.game

    private val _grids = MutableStateFlow<Set<LotoGridViewModel>>(setOf())

    val grids: StateFlow<Set<LotoGridViewModel>> = _grids.asStateFlow()

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
        game.registerPickedNumber(n)
        emit()
    }

    fun removeNumber(n: Int) {
        game.unregisterPickedNumber(n)
        emit()
    }

    fun addGrid(grid: LotoGrid) {
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
        _grids.update { game.grids.map { LotoGridViewModel(it) }.toSet() }
    }

    fun reloadGrids() {
        gridSync()
        emit()
    }

}