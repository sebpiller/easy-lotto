package ch.sebpiller.easy.loto.stats

import ch.sebpiller.easy.loto.domain.LotoGame
import java.util.*

class LotoStatisticsRecorder {

    private data class LotoStat(
        private val started: Date,
        private val finished: Date,
        private val game: LotoGame
    )

    private val games: ArrayList<LotoGame> = arrayListOf()


    fun start(game: LotoGame) {}
    fun stop() {}

    // TODO compute stats
}