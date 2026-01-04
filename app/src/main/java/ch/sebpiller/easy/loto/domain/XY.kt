package ch.sebpiller.easy.loto.domain

data class XY(val col: Int = 0, val row: Int = 0) {
    init {
        require(col in 0..8) { "col must be 0..8" }
        require(row in 0..2) { "row must be 0..2" }
    }
}