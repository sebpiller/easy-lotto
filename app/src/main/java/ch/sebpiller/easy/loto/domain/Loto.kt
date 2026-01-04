package ch.sebpiller.easy.loto.domain

class Loto {
    companion object {
        val game: LotoGame by lazy {
            var x = LotoGame()
            return@lazy x
        }
    }
}