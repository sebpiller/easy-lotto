package ch.sebpiller.easy.lotto.model

data class NumberLocation(val row: Int, val col: Int) {
    init {
        require(row in 0..2) { "row must be 0..2" }
        require(col in 0..8) { "col must be 0..8" }
    }
}