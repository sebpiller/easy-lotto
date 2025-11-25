package ch.sebpiller.easy.lotto.model

import ch.sebpiller.easy.lotto.processing.ImageReader

class LottoGrid(
    private val numbers: List<ImageReader.DetectedNumber>
) {
    companion object {
        fun fromNumbers(numbers: List<ImageReader.DetectedNumber>): LottoGrid {
            return LottoGrid(numbers)
        }
    }

    fun checkValidGrid(): Boolean {
        // 15 elements
        if (numbers.size != 15) throw IllegalStateException("grid size != 15: " + numbers.size)

        // duplicated value
        if (numbers.map { it.value }
                .toSet().size != 15) throw IllegalStateException("found duplicated value in the grid")

        // overlapping cell
        if (numbers.map { it.position }
                .toSet().size != 15) throw IllegalStateException("two cells at the same position in the grid")

        for (i in numbers) {
            // out of bounds
            if (i.value !in 1..90) throw IllegalStateException("out of range number: ${i.value}")

            // number in wrong column
            if (i.value == 90 && i.position.col != 8) throw IllegalStateException("number 90 must be located at col 9")
            else if (i.value != 90 && i.value / 10 != i.position.col) throw IllegalStateException("number ${i.value} located at wrong column: ${i.position.col}")
        }

        return true
    }

    fun findAt(row: Int, col: Int): ImageReader.DetectedNumber? {
        for (i in numbers) {
            if (i.position.col == col && i.position.row == row) return i
        }

        return null
    }

    fun printToConsole() {
        for (row in 0..2) {
            print("+ ")

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
}