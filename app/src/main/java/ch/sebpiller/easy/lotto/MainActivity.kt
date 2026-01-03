package ch.sebpiller.easy.lotto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.coroutineScope
import ch.sebpiller.easy.lotto.domain.Lotto
import ch.sebpiller.easy.lotto.domain.LottoGrid
import ch.sebpiller.easy.lotto.domain.LottoNum
import ch.sebpiller.easy.lotto.domain.XY
import ch.sebpiller.easy.lotto.ui.MainScreen
import ch.sebpiller.easy.lotto.ui.StartMenu
import ch.sebpiller.easy.lotto.ui.theme.EasyLottoTheme
import ch.sebpiller.easy.lotto.ui.viewmodel.LottoGameViewModel
import kotlinx.coroutines.async

class MainActivity : ComponentActivity() {

    val mode = "dev"

    override fun onCreate(savedInstanceState: Bundle?) {
        val loading = mutableStateOf(false)
        val vm = LottoGameViewModel()

        val x = lifecycle.coroutineScope.async {
            // Add sample test data
            if (mode == "dev" && Lotto.game.grids.isEmpty()) {
                loading.value = true


                Lotto.game.addGrid(LottoGrid.fromNumbers(
                    listOf(
                        LottoNum(9, XY(0, 0)),
                        LottoNum(28, XY(0, 2)),
                        LottoNum(31, XY(0, 3)),
                        LottoNum(47, XY(0, 4)),
                        LottoNum(72, XY(0, 7)),


                        LottoNum(21, XY(1, 2)),
                        LottoNum(37, XY(1, 3)),
                        LottoNum(54, XY(1, 5)),
                        LottoNum(61, XY(1, 6)),
                        LottoNum(85, XY(1, 8)),

                        LottoNum(6, XY(2, 0)),
                        LottoNum(15, XY(2, 1)),
                        LottoNum(40, XY(2, 4)),
                        LottoNum(60, XY(2, 6)),
                        LottoNum(70, XY(2, 7)),
                    )

                ).asValidGrid())

                Lotto.game.addGrid(LottoGrid.fromNumbers(
                    listOf(
                        LottoNum(6, XY(0, 0)),
                        LottoNum(14, XY(0, 1)),
                        LottoNum(27, XY(0, 2)),
                        LottoNum(49, XY(0, 4)),
                        LottoNum(70, XY(0, 7)),


                        LottoNum(13, XY(1, 1)),
                        LottoNum(36, XY(1, 3)),
                        LottoNum(58, XY(1, 5)),
                        LottoNum(77, XY(1, 7)),
                        LottoNum(80, XY(1, 8)),

                        LottoNum(3, XY(2, 0)),
                        LottoNum(24, XY(2, 2)),
                        LottoNum(41, XY(2, 4)),
                        LottoNum(66, XY(2, 6)),
                        LottoNum(86, XY(2, 8)),
                    )

                ).asValidGrid())

//
//                val recognizer = LottoGridRecognizer()
//                listOf("lotto_grid.webp", "lotto_grid2.jpg", "lotto_grid3.jpg").forEach { name ->
//                    val img = InputImage.fromBitmap(BitmapFactory.decodeStream(assets.open(name)), 0)
//                    val numbers = recognizer.extractAllNumbers(img)
//                    Lotto.game.addGrid(LottoGrid.fromNumbers(numbers).asValidGrid())
//                }
            }
        }
        x.invokeOnCompletion {
            vm.reloadGrids()
            loading.value = false
        }
        x.start()


        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            EasyLottoTheme {
                val showStart = remember { mutableStateOf(true) }
                val mode = remember { mutableStateOf("none") }

                if (loading.value) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Loading...", style = MaterialTheme.typography.titleLarge)
                    }
                } else if (showStart.value) {
                    StartMenu {
                        showStart.value = false
                        mode.value = "classical"
                    }
                } else {
                    if (mode.value == "classical") {
                        MainScreen(vm)
                    }
                }
            }
        }

    }
}