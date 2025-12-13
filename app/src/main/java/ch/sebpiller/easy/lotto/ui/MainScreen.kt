package ch.sebpiller.easy.lotto.ui

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.sebpiller.easy.lotto.model.LottoGame
import ch.sebpiller.easy.lotto.model.LottoGrid
import ch.sebpiller.easy.lotto.ocr.LottoGridRecognizer
import com.google.mlkit.vision.common.InputImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    menuContent: @Composable (onClose: () -> Unit) -> Unit,
    assets: AssetManager,
    vm: LottoGameViewModel = LottoGameViewModel()
) {

    val ui by vm.ui.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        val recognizer = LottoGridRecognizer()
        listOf("lotto_grid.webp", "lotto_grid2.jpg", "lotto_grid3.jpg").forEach { name ->
            val img = InputImage.fromBitmap(BitmapFactory.decodeStream(assets.open(name)), 0)
            val numbers = recognizer.extractAllNumbers(img)
            vm.addGrid(LottoGrid.fromNumbers(numbers).asValidGrid())
        }
    }

    var menuOpen by remember { mutableStateOf(false) }
    val v = remember { mutableStateOf(false) }
    val cc = remember { mutableStateOf(false) }
    val c = ConfirmDialog(
        v.value, { v.value = false }, "Next round ?",
        onConfirm = { v.value = false ; cc.value = true },
    )
    Scaffold(
        topBar = { TopAppBar(title = { Text("Welcome to EasyLotto") }) },
        bottomBar = {
            BottomAppBar(actions = {
                TextButton(onClick = {}) { Text("Most wanted: ${ui.mostWanted ?: "—"}") }
                TextButton(onClick = {
                    if (vm.ui.value.step == LottoGame.GameStep.CARTON) {
                        v.value = true
                    } else {
                        cc.value = true
                    }
                    if (cc.value) {
                        vm.nextPart()

                    }
                }) { Text("${ui.step} - Next >>") }
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
                        .fillMaxHeight(0.9f)
                        .width(300.dp)
                        .background(Color.Black.copy(alpha = 0.8f))
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
                                Row {
                                    TextButton(onClick = { menuOpen = false }) {
                                        Text(text = "Close")
                                    }
                                }
                                Row {
                                    menuContent { menuOpen = false }
                                }
                            }
                        }
                    }
                }
            }
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding).background(Color.LightGray.copy(alpha = 0.3f))
        ) {
            vm.ui.collectAsState().value.grids.forEach {
                LottoGridView(vm, it)
                Spacer(modifier = Modifier.height(20.dp))
            }
//
//            Row(
//                modifier = Modifier.padding(contentPadding).width(300.dp).height(300.dp),
//                verticalAlignment = Alignment.Bottom,
//                horizontalArrangement = Arrangement.Center,
//            ) {
//                Column {
//                    Spacer(Modifier.weight(0.1f))
//                    Text("Test1", style = MaterialTheme.typography.bodyMedium)
//                    Spacer(Modifier.height(10.dp))
//                    Text("Test2")
//                }
//                Column {
//                    Spacer(Modifier.weight(0.1f))
//                    Text("Test3")
//                    Text("Test4")
//                }
//            }

        }
    }
}






