package ch.sebpiller.easy.lotto.ui.camera

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import ch.sebpiller.easy.lotto.model.Lotto
import ch.sebpiller.easy.lotto.ocr.ImagePreparator
import ch.sebpiller.easy.lotto.ui.samples.LottoGridSamples
import ch.sebpiller.easy.lotto.ui.viewmodel.LottoGameViewModel

@Composable
fun ImagePreviewScreen(
    originalImage: Bitmap,
    croppedImage: Bitmap,
    onConfirm: (preparedImage: Bitmap) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Original", "Cropped", "Preprocessed")
    val imagePreparator = remember { ImagePreparator() }

    Box(modifier = Modifier.height(120.dp).padding(bottom = 60.dp).fillMaxWidth()) {
        Row {
            Button(
                onClick = onRetry,
            ) {
                Text("Cancel")
            }

            Button(
                onClick = { onConfirm(ImagePreparator().preprocessImage(croppedImage)) },
            ) {
                Text("Import")
            }
        }
    }

    SecondaryTabRow(
        selectedTabIndex,
        modifier = Modifier.padding(top = 120.dp),
        divider = @Composable { HorizontalDivider() },
        tabs = {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3),
    ) {
        when (selectedTabIndex) {
            0 -> {
                // Original image
                Image(
                    bitmap = originalImage.asImageBitmap(),
                    contentDescription = "Original image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Inside
                )
            }

            1 -> {
                // Prepared image (resized & contrast enhanced)
                Image(
                    bitmap = croppedImage.asImageBitmap(),
                    contentDescription = "Cropped image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Inside
                )
            }

            2 -> {
                // Extracted image (final processed)
                Image(
                    bitmap = imagePreparator.preprocessImage(croppedImage).asImageBitmap(),
                    contentDescription = "Processed image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Inside
                )
            }

        }


//            // Detection info
//            if (detectedGrid != null) {
//                Surface(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp, vertical = 8.dp),
//                    shape = MaterialTheme.shapes.medium,
//                    color = Color(0xFF2A2A2A),
//                    contentColor = Color.White
//                ) {
//                    Column(
//                        modifier = Modifier.padding(16.dp)
//                    ) {
//                        Text(
//                            text = "Grille détectée",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF4CAF50)
//                        )
//                        Text(
//                            text = "Numéros trouvés: ${detectedGrid.numbers.size}",
//                            fontSize = 14.sp,
//                            modifier = Modifier.padding(top = 8.dp),
//                            color = Color.LightGray
//                        )
//
//                        if (detectedGrid.numbers.isNotEmpty()) {
//                            Text(
//                                text = detectedGrid.numbers
//                                    .sortedBy { it.value }
//                                    .joinToString(", ") { it.value.toString() },
//                                fontSize = 12.sp,
//                                modifier = Modifier.padding(top = 8.dp),
//                                color = Color.White
//                            )
//                        }
//                    }
//                }
//            } else {
//                Surface(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp, vertical = 8.dp),
//                    shape = MaterialTheme.shapes.medium,
//                    color = Color(0xFF2A2A2A),
//                    contentColor = Color.White
//                ) {
//                    Text(
//                        text = "⚠️ Aucune grille détectée",
//                        fontSize = 14.sp,
//                        color = Color(0xFFFF6B6B),
//                        modifier = Modifier.padding(16.dp)
//                    )
//                }
//            }

        // Action buttons
    }


}


@Preview
@Composable
fun ImagePreviewScreenPreview() {
    val game = LottoGameViewModel()
    Lotto.game.addGrid(LottoGridSamples.SAMPLE1)
    game.reloadGrids()

    Surface {
        ImagePreviewScreen(
            createBitmap(100, 100),
            createBitmap(100, 100),

            {},
            {},
            Modifier
        )
    }
}