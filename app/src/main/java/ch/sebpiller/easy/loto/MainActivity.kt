package ch.sebpiller.easy.loto

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
import ch.sebpiller.easy.loto.domain.Loto
import ch.sebpiller.easy.loto.domain.LotoGrid
import ch.sebpiller.easy.loto.domain.LotoNum
import ch.sebpiller.easy.loto.domain.XY
import ch.sebpiller.easy.loto.ui.MainScreen
import ch.sebpiller.easy.loto.ui.StartMenu
import ch.sebpiller.easy.loto.ui.theme.EasyLotoTheme
import ch.sebpiller.easy.loto.ui.viewmodel.LotoGameViewModel
import kotlinx.coroutines.async

class MainActivity : ComponentActivity() {

    val mode = "dev"

    override fun onCreate(savedInstanceState: Bundle?) {
        val loading = mutableStateOf(false)
        val vm = LotoGameViewModel()

        val x = lifecycle.coroutineScope.async {
            // Add sample test data
            if (mode == "dev" && Loto.game.grids.isEmpty()) {
                loading.value = true

                Loto.game.addGrid(
                    LotoGrid.fromNumbers(
                        listOf(
                            LotoNum(10, XY(1, 0)),
                            LotoNum(31, XY(3, 0)),
                            LotoNum(41, XY(4,0)),
                            LotoNum(65, XY(6,0)),
                            LotoNum(83, XY(8,0)),


                            LotoNum(9, XY(0, 1)),
                            LotoNum(20, XY(2, 1)),
                            LotoNum(34, XY(3, 1)),
                            LotoNum(55, XY(5, 1)),
                            LotoNum(73, XY(7, 1)),

                            LotoNum(13, XY(1, row = 2)),
                            LotoNum(28, XY(2, 2)),
                            LotoNum(45, XY(4, 2)),
                            LotoNum(67, XY(6, 2)),
                            LotoNum(87, XY(8, 2)),
                        )

                    ).asValidGrid()
                )

                Loto.game.addGrid(
                    LotoGrid.fromNumbers(
                        listOf(
                            LotoNum(13, XY(1, 0)),
                            LotoNum(37, XY(3, 0)),
                            LotoNum(42, XY(4, 0)),
                            LotoNum(68, XY(6, 0)),
                            LotoNum(80, XY(8, 0)),


                            LotoNum(2, XY(0, 1)),
                            LotoNum(18, XY(1, 1)),
                            LotoNum(49, XY(4, 1)),
                            LotoNum(74, XY(7, 1)),
                            LotoNum(83, XY(8, 1)),

                            LotoNum(9, XY(0, row = 2)),
                            LotoNum(29, XY(2, 2)),
                            LotoNum(39, XY(3, 2)),
                            LotoNum(59, XY(5, 2)),
                            LotoNum(86, XY(8, 2)),
                        )

                    ).asValidGrid()
                )

                Loto.game.addGrid(
                    LotoGrid.fromNumbers(
                        listOf(
                            LotoNum(6, XY(0,0)),
                            LotoNum(22, XY(2, 0)),
                            LotoNum(31, XY(3, 0)),
                            LotoNum(50, XY(5, 0)),
                            LotoNum(70, XY(7, 0)),


                            LotoNum(12, XY(1, 1)),
                            LotoNum(34, XY(3, 1)),
                            LotoNum(51, XY(5, 1)),
                            LotoNum(62, XY(6, 1)),
                            LotoNum(77, XY(7, 1)),

                            LotoNum(14, XY(1, row = 2)),
                            LotoNum(23, XY(2, 2)),
                            LotoNum(44, XY(4, 2)),
                            LotoNum(63, XY(6, 2)),
                            LotoNum(88, XY(8, 2)),
                        )

                    ).asValidGrid()
                )

                Loto.game.addGrid(
                    LotoGrid.fromNumbers(
                        listOf(
                            LotoNum(11, XY(1,0)),
                            LotoNum(36, XY(3, 0)),
                            LotoNum(42, XY(4, 0)),
                            LotoNum(64, XY(6, 0)),
                            LotoNum(80, XY(8, 0)),


                            LotoNum(9, XY(0, 1)),
                            LotoNum(23, XY(2, 1)),
                            LotoNum(39, XY(3, 1)),
                            LotoNum(51, XY(5, 1)),
                            LotoNum(77, XY(7, 1)),

                            LotoNum(17, XY(1, row = 2)),
                            LotoNum(28, XY(2, 2)),
                            LotoNum(47, XY(4, 2)),
                            LotoNum(65, XY(6, 2)),
                            LotoNum(81, XY(8, 2)),
                        )

                    ).asValidGrid()
                )

//
//                val recognizer = LotoGridRecognizer()
//                listOf("loto_grid.webp", "loto_grid2.jpg", "loto_grid3.jpg").forEach { name ->
//                    val img = InputImage.fromBitmap(BitmapFactory.decodeStream(assets.open(name)), 0)
//                    val numbers = recognizer.extractAllNumbers(img)
//                    Loto.game.addGrid(LotoGrid.fromNumbers(numbers).asValidGrid())
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
            EasyLotoTheme {
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