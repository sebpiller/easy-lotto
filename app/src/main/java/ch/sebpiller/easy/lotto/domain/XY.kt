package ch.sebpiller.easy.lotto.domain

data class XY(val row: Int = 0, val col: Int = 0) {
    init {
        require(row in 0..2) { "row must be 0..2" }
        require(col in 0..8) { "col must be 0..8" }
    }
}