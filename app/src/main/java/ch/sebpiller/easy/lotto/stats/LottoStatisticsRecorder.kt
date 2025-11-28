package ch.sebpiller.easy.lotto.stats

import ch.sebpiller.easy.lotto.model.LottoGame
import java.util.*

class LottoStatisticsRecorder {

    private data class LottoStat(
        private val started: Date,
        private val finished: Date,
        private val game: LottoGame
    )

    private val games: ArrayList<LottoGame> = arrayListOf()


    fun start(game: LottoGame) {}
    fun stop() {}

    // TODO compute stats
}