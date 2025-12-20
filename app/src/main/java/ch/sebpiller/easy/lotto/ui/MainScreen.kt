package ch.sebpiller.easy.lotto.ui

import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.sebpiller.easy.lotto.model.Lotto
import ch.sebpiller.easy.lotto.model.LottoGame
import ch.sebpiller.easy.lotto.model.LottoGrid
import ch.sebpiller.easy.lotto.ui.samples.LottoGridSamples
import ch.sebpiller.easy.lotto.ui.tools.ConfirmDialog
import ch.sebpiller.easy.lotto.ui.viewmodel.LottoGameViewModel
import kotlin.system.exitProcess


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: LottoGameViewModel) {
    val scope = rememberCoroutineScope()
    val showScanner = remember { mutableStateOf(false) }
    val capturedBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val croppedBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val processedBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val showCropper = remember { mutableStateOf(false) }
    val showPreview = remember { mutableStateOf(false) }
    val detectedGrid = remember { mutableStateOf<LottoGrid?>(null) }
    val localContext = LocalContext.current

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

        // Caméra
        if (showScanner.value && !showCropper.value && !showPreview.value) {
            val imageCaptureUseCase = remember { ImageCapture.Builder().build() }

            Column(modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3)) {
                CameraPreviewScreen(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    imageCapture = imageCaptureUseCase
                )
                Row(modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)) {
                    Button(onClick = { showScanner.value = false }) { Text("Annuler") }
                    Button(onClick = {
                        val callback = object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                super.onCaptureSuccess(image)
                                capturedBitmap.value = image.toBitmap()
                                showCropper.value = true
                            }

                            override fun onError(exception: ImageCaptureException) {}
                        }

                        imageCaptureUseCase.takePicture(
                            ContextCompat.getMainExecutor(localContext),
                            callback
                        )
                    }) { Text("Prendre la photo") }
                }
            }
        }

        // Recadrage
        if (showCropper.value && capturedBitmap.value != null) {
            ImageCropperScreen(
                modifier = Modifier.fillMaxWidth(),
                bitmap = capturedBitmap.value!!,
                onCropConfirmed = {
                    croppedBitmap.value = it!!
                    showCropper.value = false
                    showPreview.value = true

                },
                onCancel = {
                    showCropper.value = false
                    showPreview.value = false
                },
            )
        }

        // Prévisualisation
        if (showPreview.value && croppedBitmap.value != null) {
            ImagePreviewScreen(
                croppedImage = croppedBitmap.value!!,
             //   processedBitmap = processedBitmap.value!!,
                detectedGrid = detectedGrid.value,
                onConfirm = {
                    if (detectedGrid.value != null) {
                        vm.addGrid(detectedGrid.value!!)
                    }

                    // Réinitialiser les états
                    showPreview.value = false
                    showScanner.value = false
                    capturedBitmap.value = null
                    croppedBitmap.value = null
                    processedBitmap.value = null
                    detectedGrid.value = null
                },
                onRetry = {
                    showPreview.value = false
                    showCropper.value = true
                    capturedBitmap.value = croppedBitmap.value
                }
            )
        }
    }
}

@Composable
fun MainScreenPreview() {
    val game = LottoGameViewModel()
    Lotto.game.addGrid(LottoGridSamples.SAMPLE1)
    game.reloadGrids()

    Surface {
        MainScreen(game)
    }
}
