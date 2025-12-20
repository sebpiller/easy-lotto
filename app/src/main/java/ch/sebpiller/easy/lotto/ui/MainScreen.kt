package ch.sebpiller.easy.lotto.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.sebpiller.easy.lotto.model.Lotto
import ch.sebpiller.easy.lotto.model.LottoGame
import ch.sebpiller.easy.lotto.model.LottoGrid
import ch.sebpiller.easy.lotto.ocr.LottoGridRecognizer
import ch.sebpiller.easy.lotto.ui.samples.LottoGridSamples
import ch.sebpiller.easy.lotto.ui.tools.ConfirmDialog
import ch.sebpiller.easy.lotto.ui.viewmodel.LottoGameViewModel
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: LottoGameViewModel) {
    val scope = rememberCoroutineScope()
    val showScanner = remember { mutableStateOf(false) }
    if (showScanner.value) {
        TakePhotoCropperDialog(
            onDismissRequest = { showScanner.value = false },
            onResult = { bmp ->
                scope.launch {
                    val img = InputImage.fromBitmap(bmp, 0)
                    val numbers = LottoGridRecognizer().extractAllNumbers(img)
                    vm.addGrid(LottoGrid.fromNumbers(numbers).asValidGrid())
                    showScanner.value = false
                }
            }
        )
    }

    val ui by vm.ui.collectAsStateWithLifecycle()

    // FIXME generalize confirmdialog
    val showConfirm = remember { mutableStateOf(false) }
    val showConfirmQuestion = remember { mutableStateOf("") }
    val showConfirmMessage = remember { mutableStateOf(null as String?) }
    val confirmCallback = remember { mutableStateOf({}) }

    ConfirmDialog(
        visible = showConfirm.value,
        title = showConfirmQuestion.value,
        message = showConfirmMessage.value,
        onDismiss = {
            showConfirm.value = false
        },
        onConfirm = {
            showConfirm.value = false;
            confirmCallback.value.invoke()
        },
    )

    fun ifConfirm(question: String, details: String? = null, onConfirm: () -> Unit) {
        confirmCallback.value = onConfirm
        showConfirmQuestion.value = question
        showConfirmMessage.value = details
        showConfirm.value = true
    }

    var menuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Welcome to EasyLotto - CLASSICAL") }) },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.height(IntrinsicSize.Min),
                actions = {
                    Row(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.85f)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row {
                                RadioButton(ui.step == LottoGame.GameStep.QUINE, {})
                                RadioButton(ui.step == LottoGame.GameStep.DQUINE, {})
                                RadioButton(ui.step == LottoGame.GameStep.CARTON, {})
                            }

                            AnimatedVisibility(
                                visible = ui.mostWanted != null,
                                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)),
                                exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(200)),
                            ) {
                                Text(
                                    "HOT!! ${ui.mostWanted} !!HOT",
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.bodyMedium.plus(
                                        TextStyle(
                                            color = Color.Red,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Button(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                onClick = {
                                    if (vm.ui.value.step != LottoGame.GameStep.CARTON) {
                                        vm.nextPart()
                                    } else {
                                        ifConfirm(
                                            "Play again ?",
                                            "This game has reached the end...\nStart a new one from the beginning ?"
                                        ) {
                                            vm.reset()
                                            vm.grids.value.forEach { it.resetCheckNumbers() }
                                        }
                                    }
                                }) { Text(if (ui.step != LottoGame.GameStep.CARTON) "Next >>" else "Finish !") }
                        }

                        Column(modifier = Modifier.weight(0.66f)) {
                            Button(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                onClick = {
                                    ifConfirm("Are you sure ?", "Remove all ticked numbers, start at the beginning") {
                                        vm.reset()
                                        vm.grids.value.forEach { it.resetCheckNumbers() }
                                    }
                                }) { Text("Reset") }
                        }
                    }
                })
        },

        snackbarHost = { SnackbarHost(hostState = SnackbarHostState()) },
        floatingActionButtonPosition = FabPosition.EndOverlay,
        floatingActionButton = {
            SmallFloatingActionButton(onClick = { menuOpen = true }) {
                Text(text = "≡", style = MaterialTheme.typography.titleMedium)
            }

            if (menuOpen) {
                Column(
                    modifier = Modifier
                        .clickable(true) {
                            menuOpen = false
                        }) {
                    AnimatedVisibility(
                        visible = menuOpen,
                        enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(200)),
                        exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(200)),
                    ) {
                        Surface {
                            Column {
                                Button(onClick = {
                                    showScanner.value = true
                                }) { Text("Scan new grid") }

                                Button(onClick = {
                                    ifConfirm(
                                        "Really ?",
                                        "This will remove all the grids and you will have to scan them again !"
                                    ) {
                                        vm.removeAllGrids()
                                    }
                                }) { Text("Remove all grids") }

                                Button(onClick = {
                                    ifConfirm("Quit ?", "We will miss you !") {
                                        exitProcess(0)
                                    }
                                }) { Text("Quit") }
                            }
                        }
                    }
                }
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding).padding(16.dp),
        ) {
            vm.ui.collectAsState().value.grids.forEach {
                LottoGridView(vm, it)
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}



@Composable
@Preview
fun MainScreenPreview() {
    val game = LottoGameViewModel()
    Lotto.game.addGrid(LottoGridSamples.SAMPLE1)
    game.reloadGrids()

    Surface {
        MainScreen(game)
    }
}
