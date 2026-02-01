package ch.sebpiller.easy.loto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.sebpiller.easy.loto.domain.LotoNum
import ch.sebpiller.easy.loto.ui.samples.LotoGridSamples
import ch.sebpiller.easy.loto.ui.viewmodel.LotoGameViewModel
import ch.sebpiller.easy.loto.ui.viewmodel.LotoGridViewModel

@Composable
fun LotoGridView(
    game: LotoGameViewModel,
    viewModel: LotoGridViewModel,
    onGameCallback: Function1<LotoGameViewModel, Unit>? = {},
) {
    val positions = (0..2).flatMap { r -> (0..8).map { c -> r to c } }

    val backgroundColor = Color.Blue.copy(alpha = .5f)
    val containerColor = backgroundColor.copy(green = 0.5f, red = 0.5f)
    LazyVerticalGrid(
        columns = GridCells.Fixed(9),
        modifier = Modifier
            .widthIn(min = 100.dp, max = 60000.dp)
    ) {
        items(positions) { (row, col) ->
            val num: LotoNum? = viewModel.grid.findAt(row, col)


            Card(
                modifier = Modifier
                    .aspectRatio(0.85f)
                    .padding(1.dp)
                    .border(1.dp, Color.Black, RoundedCornerShape(4.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = containerColor,
                ),
            ) {
                if (num != null) {
                    val isHighlighted = viewModel.checkNumbers.collectAsState().value.contains(num.value)

                    TextButton(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor)
                            .border(1.dp, Color.Black, RoundedCornerShape(4.dp)),

                        onClick = {
                            game.ui.value.numbers.contains(num.value).let {
                                if (it)
                                    game.removeNumber(num.value)
                                else
                                    game.pushNumber(num.value)
                            }
                            onGameCallback?.invoke(game)
                        },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (isHighlighted) Color.DarkGray else Color.Transparent,
                            contentColor = if (isHighlighted) Color.White else Color.Black,
                        ),
                    ) {
                        Text(
                            text = num.value.toString(),
                            softWrap = false,
                            textAlign = TextAlign.Center,
                            autoSize = TextAutoSize.StepBased(),
                            modifier = Modifier
                                .align(CenterVertically)
                                .weight(FontWeight.Bold.weight.toFloat())

                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun LotoGridViewPreview() {
    val viewModel = LotoGridViewModel(LotoGridSamples.SAMPLE1)

    Surface {
        LotoGridView(game = LotoGameViewModel(), viewModel = viewModel)
    }
}
