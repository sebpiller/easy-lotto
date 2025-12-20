package ch.sebpiller.easy.lotto

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.coroutineScope
import ch.sebpiller.easy.lotto.model.Lotto
import ch.sebpiller.easy.lotto.model.LottoGrid
import ch.sebpiller.easy.lotto.ocr.LottoGridRecognizer
import ch.sebpiller.easy.lotto.ui.viewmodel.LottoGameViewModel
import ch.sebpiller.easy.lotto.ui.MainScreen
import ch.sebpiller.easy.lotto.ui.StartMenu
import ch.sebpiller.easy.lotto.ui.theme.EasyLottoTheme
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.async

class MainActivity : ComponentActivity() {

    val mode = "fdev"

    override fun onCreate(savedInstanceState: Bundle?) {
        val loading = mutableStateOf(false)
        val vm = LottoGameViewModel()

        val x = lifecycle.coroutineScope.async {
            // Add sample test data
            if (mode == "dev" && Lotto.game.grids.isEmpty()) {
                loading.value = true
                val recognizer = LottoGridRecognizer()
                listOf("lotto_grid.webp", "lotto_grid2.jpg", "lotto_grid3.jpg").forEach { name ->
                    val img = InputImage.fromBitmap(BitmapFactory.decodeStream(assets.open(name)), 0)
                    val numbers = recognizer.extractAllNumbers(img)
                    Lotto.game.addGrid(LottoGrid.fromNumbers(numbers).asValidGrid())
                }
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