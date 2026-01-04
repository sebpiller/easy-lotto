package ch.sebpiller.easy.loto.ui.viewmodel

import androidx.lifecycle.ViewModel
import ch.sebpiller.easy.loto.domain.LotoGame
import ch.sebpiller.easy.loto.domain.LotoGrid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LotoGridViewModel(
    val grid: LotoGrid,
) : ViewModel() {

    private val _checkedNumbers = MutableStateFlow<Set<Int>>(emptySet())
    val checkNumbers: StateFlow<Set<Int>> = _checkedNumbers.asStateFlow()


    fun toggleCheckNum(num: Int) {
        _checkedNumbers.update { current ->
            if (current.contains(num)) current - num else current + num
        }
    }

    fun syncCheckedNumbers(game: LotoGame) {
        //Log.d("LGVM", "syncCheckedNumbers")
        _checkedNumbers.update { HashSet(game.pickedNumbers) }
    }

    fun resetCheckNumbers() {
        _checkedNumbers.update { setOf() }
    }

}