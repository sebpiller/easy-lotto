package ch.sebpiller.easy.loto.domain

class LotoGame {
    enum class GameStep(private val rowsToWin: Int, val label: String) {
        QUINE(1, "X"),
        DQUINE(2, "XX"),
        CARTON(3, "XXX");

        fun isWinWithRows(rowFull: Int): Boolean {
            return rowFull >= rowsToWin
        }
    }

    var step = GameStep.QUINE
    val pickedNumbers: MutableSet<Int> = HashSet(90)
    val grids: MutableList<LotoGrid> = ArrayList()

    fun removeAllGrids() {
        grids.clear()
    }

    fun addGrid(grid: LotoGrid) {
        grids.add(grid)
    }

    fun reset() {
        step = GameStep.QUINE
        pickedNumbers.clear()
    }

    fun nextPart() {
        if (step == GameStep.CARTON) pickedNumbers.clear()

        step = when (step) {
            GameStep.QUINE -> GameStep.DQUINE
            GameStep.DQUINE -> GameStep.CARTON
            GameStep.CARTON -> GameStep.QUINE
        }

    }

    fun unregisterPickedNumber(number: Int) {
        require(number in 1..90) { "number must be between 1 and 90" }

        if (!pickedNumbers.remove(number)) {
            System.err.println("the number $number was not in picked numbers !")
        }
    }

    fun registerPickedNumber(number: Int) {
        require(number in 1..90) { "number must be between 1 and 90" }

        if (!pickedNumbers.add(number)) {
            System.err.println("the number $number has already been given !")
        }
    }

    // search for the most wanted number if one can make you win the game !
    fun mostWantedNumber(): Int? {
        for (g in grids) {
            val mostWantedNumber = g.mostWantedNumber(pickedNumbers, step)
            if (mostWantedNumber != null)
                return mostWantedNumber
        }
        return null
    }

    fun checkWin(): Boolean {
        for (grid in grids) {
            var rowFull = 0

            for (row in 0..2) {
                var foundNOnRow = 0

                for (col in 0..8) {
                    val n = grid.findAt(row, col)

                    if (n != null && pickedNumbers.contains(n.value)) {
                        foundNOnRow++
                    }
                }

                if (foundNOnRow == 5) {
                    rowFull++
                }
            }

            if (step.isWinWithRows(rowFull)) {
                // you win !
                return true
            }
        }

        return false
    }
}
