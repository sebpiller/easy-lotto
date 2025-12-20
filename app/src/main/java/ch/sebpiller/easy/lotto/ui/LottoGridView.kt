package ch.sebpiller.easy.lotto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.sebpiller.easy.lotto.model.LottoNumber
import ch.sebpiller.easy.lotto.ui.samples.LottoGridSamples
import ch.sebpiller.easy.lotto.ui.viewmodel.LottoGameViewModel
import ch.sebpiller.easy.lotto.ui.viewmodel.LottoGridViewModel

@Composable
fun LottoGridView(
    game: LottoGameViewModel,
    viewModel: LottoGridViewModel,
) {
    val positions = (0..2).flatMap { r -> (0..8).map { c -> r to c } }

    LazyVerticalGrid(
        columns = GridCells.Fixed(9),
        modifier = Modifier
            .background(Color.Green.copy(alpha = 0.1f))
            .border(2.dp, Color.Gray, RoundedCornerShape(3.dp))
            .widthIn(min = 100.dp, max = 400.dp)
            .aspectRatio(3f)
    ) {
        items(positions) { (row, col) ->
            val num: LottoNumber? = viewModel.grid.findAt(row, col)

            Card(
                modifier = Modifier.aspectRatio(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
                ),
            ) {
                if (num != null) {
                    val isHighlighted = viewModel.checkNumbers.collectAsState().value.contains(num.value)

                    TextButton(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .align(Alignment.CenterHorizontally)
                            .padding(1.dp),
                        onClick = {
                            viewModel.toggleCheckNum(num.value)
                            game.pushNumber(num.value)
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
                                .background(Color.Transparent)
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
    val viewModel = LottoGridViewModel(LottoGridSamples.SAMPLE1)

    Surface {
        LottoGridView(game = LottoGameViewModel(), viewModel = viewModel)
    }
}
