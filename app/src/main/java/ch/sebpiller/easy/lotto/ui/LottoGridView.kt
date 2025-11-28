package ch.sebpiller.easy.lotto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.sebpiller.easy.lotto.model.LottoGrid
import ch.sebpiller.easy.lotto.model.LottoNumber
import ch.sebpiller.easy.lotto.model.NumberLocation

/**
 * Visual renderer for a 3x9 Lotto grid.
 *
 * Shows a 3 rows by 9 columns board. Each cell either displays the number assigned
 * at that [row, col] or stays empty. Optionally highlights numbers that appear in
 * [highlightedValues].
 */
@Composable
fun LottoGridView(
    grid: LottoGrid,
    highlightedValues: MutableSet<Int> = mutableSetOf()
) {
    val positions = (0..2).flatMap { r -> (0..8).map { c -> r to c } }

    LazyVerticalGrid(
        columns = GridCells.Fixed(9),
        modifier = Modifier
            .background(Color.Blue.copy(alpha = 0.3f))
            .border(1.dp, Color.Yellow, RoundedCornerShape(3.dp))
            .padding(3.dp)
    ) {
        items(positions) { (row, col) ->
            val num: LottoNumber? = grid.findAt(row, col)

            Card(
                modifier = Modifier.aspectRatio(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
                ),
            ) {
                if (num != null) {
                    val isHighlighted = highlightedValues.contains(num.value)

                    TextButton(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .align(Alignment.CenterHorizontally),
                        onClick = {
                            if (isHighlighted) highlightedValues.remove(num.value) else highlightedValues.add(num.value)
                        },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (isHighlighted) Color.DarkGray else Color.LightGray,
                            contentColor = if (isHighlighted) Color.White else Color.Black,
                        ),
                    ) {

                        Text(
                            text = num.value.toString(),
                            softWrap = false,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(CenterVertically)
                                .background(Color.Transparent),

                            )
                    }
                }

            }
        }
    }
}

@Composable
@Preview
fun LottoGridViewPreview() {
    val sample = LottoGrid.fromNumbers(
        listOf(
            // Row 0
            LottoNumber(1, NumberLocation(0, 0)),
            LottoNumber(12, NumberLocation(0, 1)),
            LottoNumber(26, NumberLocation(0, 2)),
            LottoNumber(40, NumberLocation(0, 4)),
            LottoNumber(90, NumberLocation(0, 8)),
            // Row 1
            LottoNumber(5, NumberLocation(1, 0)),
            LottoNumber(18, NumberLocation(1, 1)),
            LottoNumber(29, NumberLocation(1, 2)),
            LottoNumber(55, NumberLocation(1, 5)),
            LottoNumber(63, NumberLocation(1, 6)),
            // Row 2
            LottoNumber(7, NumberLocation(2, 0)),
            LottoNumber(21, NumberLocation(2, 2)),
            LottoNumber(33, NumberLocation(2, 3)),
            LottoNumber(47, NumberLocation(2, 4)),
            LottoNumber(84, NumberLocation(2, 8)),
        )
    )

    Surface() {
        LottoGridView(
            grid = sample, highlightedValues = mutableSetOf(12, 47, 90)
        )
    }
}
