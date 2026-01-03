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

                Lotto.game.addGrid(
                    LottoGrid.fromNumbers(
                        listOf(
                            LottoNum(10, XY(1, 0)),
                            LottoNum(31, XY(3, 0)),
                            LottoNum(41, XY(4,0)),
                            LottoNum(65, XY(6,0)),
                            LottoNum(83, XY(8,0)),


                            LottoNum(9, XY(0, 1)),
                            LottoNum(20, XY(2, 1)),
                            LottoNum(34, XY(3, 1)),
                            LottoNum(55, XY(5, 1)),
                            LottoNum(73, XY(7, 1)),

                            LottoNum(13, XY(1, row = 2)),
                            LottoNum(28, XY(2, 2)),
                            LottoNum(45, XY(4, 2)),
                            LottoNum(67, XY(6, 2)),
                            LottoNum(87, XY(8, 2)),
                        )

                    ).asValidGrid()
                )

                Lotto.game.addGrid(
                    LottoGrid.fromNumbers(
                        listOf(
                            LottoNum(13, XY(1, 0)),
                            LottoNum(37, XY(3, 0)),
                            LottoNum(42, XY(4, 0)),
                            LottoNum(68, XY(6, 0)),
                            LottoNum(80, XY(8, 0)),


                            LottoNum(2, XY(0, 1)),
                            LottoNum(18, XY(1, 1)),
                            LottoNum(49, XY(4, 1)),
                            LottoNum(74, XY(7, 1)),
                            LottoNum(83, XY(8, 1)),

                            LottoNum(9, XY(0, row = 2)),
                            LottoNum(29, XY(2, 2)),
                            LottoNum(39, XY(3, 2)),
                            LottoNum(59, XY(5, 2)),
                            LottoNum(86, XY(8, 2)),
                        )

                    ).asValidGrid()
                )

                Lotto.game.addGrid(
                    LottoGrid.fromNumbers(
                        listOf(
                            LottoNum(6, XY(0,0)),
                            LottoNum(22, XY(2, 0)),
                            LottoNum(31, XY(3, 0)),
                            LottoNum(50, XY(5, 0)),
                            LottoNum(70, XY(7, 0)),


                            LottoNum(12, XY(1, 1)),
                            LottoNum(34, XY(3, 1)),
                            LottoNum(51, XY(5, 1)),
                            LottoNum(62, XY(6, 1)),
                            LottoNum(77, XY(7, 1)),

                            LottoNum(14, XY(1, row = 2)),
                            LottoNum(23, XY(2, 2)),
                            LottoNum(44, XY(4, 2)),
                            LottoNum(63, XY(6, 2)),
                            LottoNum(88, XY(8, 2)),
                        )

                    ).asValidGrid()
                )

                Lotto.game.addGrid(
                    LottoGrid.fromNumbers(
                        listOf(
                            LottoNum(11, XY(1,0)),
                            LottoNum(36, XY(3, 0)),
                            LottoNum(42, XY(4, 0)),
                            LottoNum(64, XY(6, 0)),
                            LottoNum(80, XY(8, 0)),


                            LottoNum(9, XY(0, 1)),
                            LottoNum(23, XY(2, 1)),
                            LottoNum(39, XY(3, 1)),
                            LottoNum(51, XY(5, 1)),
                            LottoNum(77, XY(7, 1)),

                            LottoNum(17, XY(1, row = 2)),
                            LottoNum(28, XY(2, 2)),
                            LottoNum(47, XY(4, 2)),
                            LottoNum(65, XY(6, 2)),
                            LottoNum(81, XY(8, 2)),
                        )

                    ).asValidGrid()
                )

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