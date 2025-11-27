package ch.sebpiller.easy.lotto.model

class LottoGame {
    enum class GameStep(private val rowsToWin: Int) {
        QUINE(1),
        DQUINE(2),
        CARTON(3);

        fun canBeWinWithRows(rowFull: Int): Boolean {
            return rowFull >= rowsToWin
        }
    }

    private var step = GameStep.QUINE
    private val numbers: MutableSet<Int> = HashSet(90)
    private val userGrids: MutableList<LottoGrid> = ArrayList()

    fun removeAllGrids() {
        userGrids.clear()
    }

    fun addGrid(grid: LottoGrid) {
        userGrids.add(grid)
    }

    fun reset() {
        step = GameStep.QUINE
        numbers.clear()
    }

    fun nextPart() {
        step = when (step) {
            GameStep.QUINE -> GameStep.DQUINE
            GameStep.DQUINE -> GameStep.CARTON
            GameStep.CARTON -> GameStep.QUINE
        }
    }

    fun pushNumber(number: Int) {
        require(number in 1..90) { "number must be between 1 and 90" }

        if (!numbers.add(number)) {
            System.err.println("the number $number has already been given !")
        }
    }

    // search for the most wanted number if one can make you win the game !
    fun mostWantedNumber(): Int? {
        for (g in userGrids) {
            val mostWantedNumber = g.mostWantedNumber(numbers, step)
            if (mostWantedNumber != null)
                return mostWantedNumber
        }
        return null
    }

    fun checkWin(): Boolean {
        for (grid in userGrids) {
            var rowFull = 0

            for (row in 0..2) {
                var foundNOnRow = 0

                for (col in 0..8) {
                    val n = grid.findAt(row, col)

                    if (n != null && numbers.contains(n.value)) {
                        foundNOnRow++
                    }
                }

                if (foundNOnRow == 5) {
                    rowFull++
                }
            }

            if (step.canBeWinWithRows(rowFull)) {
                // you win !
                return true
            }
        }

        return false
    }
}
