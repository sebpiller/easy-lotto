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
import ch.sebpiller.easy.lotto.model.LottoGrid
import ch.sebpiller.easy.lotto.ocr.LottoGridRecognizer
import com.google.mlkit.vision.common.InputImage

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    menuContent: @Composable (onClose: () -> Unit) -> Unit,
    assets: AssetManager,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {

            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .padding(5.dp)
            ) {
                val recognizer = remember { LottoGridRecognizer() }
                val grids = remember { mutableStateListOf<LottoGrid>() }
                val highlightStates = remember { mutableStateListOf<MutableSet<Int>>() }

                // TODO temp to replace
                LaunchedEffect(Unit) {
                    val names = listOf("lotto_grid.webp", "lotto_grid2.jpg", "lotto_grid3.jpg")
                    for (name in names) {
                        val img = InputImage.fromBitmap(
                            BitmapFactory.decodeStream(assets.open(name)), 0
                        )

                        val numbers = recognizer.extractAllNumbers(img)
                        val grid = LottoGrid.fromNumbers(numbers).asValidGrid()
                        grids.add(grid)
                    }
                }

                if (highlightStates.size < grids.size) {
                    repeat(grids.size - highlightStates.size) {
                        val x = remember { mutableStateSetOf<Int>() }
                        highlightStates.add(x)
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))
                Text(text = "Welcome to EasyLotto", modifier = Modifier)
                Spacer(modifier = Modifier.height(40.dp))

                grids.forEachIndexed { index, grid ->
                    val y = highlightStates[index]
                    LottoGridView(grid = grid, highlightedValues = y)
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

        // Top-right small button to open the menu
        SmallFloatingActionButton(
            onClick = { menuOpen = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(18.dp)
        ) {
            Text(
                text = "≡",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Scrim when open
        if (menuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable { menuOpen = false }
            )
        }

        // Right overlay menu
        AnimatedVisibility(
            visible = menuOpen,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(200)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(200)),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Menu", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { menuOpen = false }) { Text("Close") }
                    }

                    // Let caller define the menu controls
                    menuContent { menuOpen = false }
                }
            }
        }
    }
}
