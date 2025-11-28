package ch.sebpiller.easy.lotto.model

class Lotto {
    companion object {
        val game: LottoGame by lazy {
            var x = LottoGame()
            return@lazy x
        }
    }
}