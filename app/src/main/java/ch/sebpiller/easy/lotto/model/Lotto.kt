package ch.sebpiller.easy.lotto.model

class Lotto {
    companion object {
        val game: LottoGame by lazy {
            LottoGame()
        }
    }
}