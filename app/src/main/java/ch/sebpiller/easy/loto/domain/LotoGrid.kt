package ch.sebpiller.easy.loto.domain

class LotoGrid(
    val numbers: List<LotoNum>
) {
    companion object {
        fun fromNumbers(numbers: List<LotoNum>): LotoGrid {
            return LotoGrid(numbers)
        }
    }

    fun asValidGrid(): LotoGrid {
        // 15 elements
        check(numbers.size == 15) { "must have 15 numbers but have ${numbers.size}" }

        // duplicated value
        check(numbers.map { it.value }
            .toSet().size == 15) { "found duplicated value in the grid" }

        // 5 numbers on each row
        for (row in 0..2) {
            var c = 0

            for (col in 0..8) {
                if (findAt(row, col) != null)
                    c++
            }

            check(c == 5) { "found $c number at row $row instead of 5" }
        }

        // overlapping cell
        check(numbers.map { it.position }
            .toSet().size == 15) { "two cells at the same position in the grid" }

        for (i in numbers) {
            // out of range
            check(i.value in 1..90) { "out of range number: ${i.value} " }

            // number in wrong column
            check(!(i.value == 90 && i.position.col != 8)) { "number 90 must be located at col 9" }
            check(!(i.value != 90 && i.value / 10 != i.position.col)) { "number ${i.value} located at wrong column: ${i.position.col}" }
        }

        return this
    }

    fun findAt(row: Int, col: Int): LotoNum? {
        for (i in numbers) {
            if (i.position.col == col && i.position.row == row) return i
        }

        return null
    }

    fun print() {
        for (row in 0..2) {
            print("+ | ")

            for (col in 0..8) {
                val f = findAt(row, col)
                if (f == null) {
                    print("  ")
                } else
                    print(if (f.value < 10) " " + f.value else f.value)
                print(" | ")
            }

            println("+")
        }

    }

    fun mostWantedNumber(numbers: Set<Int>, step: LotoGame.GameStep): Int? {
        var mmm: Int? = null
        var fullRows = 0
        for (row in 0..2) {
            var cols = 0
            var most: Int? = null

            for (col in 0..8) {
                val f = findAt(row, col)

                if (f != null) {
                    if (numbers.contains(f.value))
                        cols++
                    else
                        most = f.value
                }
            }

            if (cols == 5)
                fullRows++
            else if (cols == 4)
                mmm = most
        }

        if (step == LotoGame.GameStep.QUINE && fullRows <= 0)
            return mmm

        if (step == LotoGame.GameStep.DQUINE && fullRows >= 1)
            return mmm

        if (step == LotoGame.GameStep.CARTON && fullRows == 2)
            return mmm

        return null
    }
}