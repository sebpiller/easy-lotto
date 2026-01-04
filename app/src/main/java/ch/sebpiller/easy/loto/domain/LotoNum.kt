package ch.sebpiller.easy.loto.domain


data class LotoNum(
    val value: Int, val position: XY
) {
    init {
        require(value in 1..90) { "invalid number, must be 1..90" }
    }
}
