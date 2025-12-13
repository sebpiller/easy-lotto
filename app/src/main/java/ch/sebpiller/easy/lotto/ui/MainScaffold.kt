package ch.sebpiller.easy.lotto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MainScaffold(
    ui: GameUiState,
    vm: LottoGameViewModel,
    menuOpen: MutableState<Boolean>,
    menuContent: @Composable ((onClose: () -> Unit) -> Unit),
) {
    ModalNavigationDrawer(
        drawerState = rememberDrawerState(if (menuOpen.value) DrawerValue.Open else DrawerValue.Closed),
        drawerContent = {
            ModalDrawerSheet { menuContent { menuOpen.value = false } }
        }
    ) {
        Scaffold(
            topBar = {
                Box {
                    Text(text = "Welcome to EasyLotto", modifier = Modifier.align(Alignment.BottomEnd))
                }
            },
//            bottomBar = { BottomBar(ui, onNext = vm::nextPart) },

            floatingActionButton = {
                SmallFloatingActionButton(onClick = { menuOpen.value = true }) {
                    Text(text = "≡", style = MaterialTheme.typography.titleMedium)
                }
            },
            floatingActionButtonPosition = FabPosition.EndOverlay,
        ) {
            content(vm = vm)
//
//            Row(
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

@Composable
fun content(
    vm: LottoGameViewModel,
) {
    Column(
        modifier = Modifier.background(Color.LightGray.copy(alpha = 0.3f)),
    ) {
        vm.ui.collectAsState().value.grids.forEach {
            LottoGridView(vm, it)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

