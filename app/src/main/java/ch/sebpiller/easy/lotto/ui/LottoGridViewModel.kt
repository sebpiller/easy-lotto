package ch.sebpiller.easy.lotto.ui

import androidx.lifecycle.ViewModel
import ch.sebpiller.easy.lotto.model.LottoGrid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LottoGridViewModel(
    val grid: LottoGrid,
) : ViewModel() {

    private val _checkedNumbers = MutableStateFlow<Set<Int>>(emptySet())
    val checkNumbers: StateFlow<Set<Int>> = _checkedNumbers.asStateFlow()


    fun toggleCheckNum(num: Int) {
        _checkedNumbers.update { current ->
            if (current.contains(num)) current - num else current + num
        }
    }

}