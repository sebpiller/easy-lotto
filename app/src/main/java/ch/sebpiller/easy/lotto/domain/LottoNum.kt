package ch.sebpiller.easy.lotto.domain


data class LottoNum(
    val value: Int, val position: XY
) {
    init {
        require(value in 1..90) { "invalid number, must be 1..90" }
    }
}
