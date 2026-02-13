package ch.sebpiller.easy.loto.ui.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.sebpiller.easy.loto.ocr.ImagePreparator

@Composable
fun ImagePreviewScreen(
    originalImage: Bitmap,
    croppedImage: Bitmap,
    onConfirm: (preparedImage: Bitmap) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Original", "Cropped", "Preprocessed")
    val imagePreparator = remember { ImagePreparator() }


    SecondaryTabRow (
        modifier = modifier,
        selectedTabIndex = selectedTabIndex,
        tabs = {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
    )


        when (selectedTabIndex) {
            0 -> {
                // Original image
                Image(
                    bitmap = originalImage.asImageBitmap(),
                    contentDescription = "Original image",
                    modifier = modifier.paddingFromBaseline(top = 50.dp).fillMaxWidth().aspectRatio(4f / 3),
                    contentScale = ContentScale.Fit
                )
            }

            1 -> {
                // Prepared image (resized & contrast enhanced)
                Image(
                    bitmap = croppedImage.asImageBitmap(),
                    contentDescription = "Cropped image",
                    modifier = modifier.paddingFromBaseline(top = 50.dp).fillMaxWidth().aspectRatio(4f / 3),
                    contentScale = ContentScale.Fit
                )
            }

            2 -> {
                // Extracted image (final processed)
                Image(
                    bitmap = imagePreparator.preprocessImage(croppedImage).asImageBitmap(),
                    contentDescription = "Processed image",
                    modifier = modifier.paddingFromBaseline(top = 50.dp).fillMaxWidth().aspectRatio(4f / 3),
                    contentScale = ContentScale.Fit
                )
            }

        }


    Row(modifier = modifier.paddingFromBaseline(top = 300.dp, bottom = 32.dp)) {

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


}

@Preview(apiLevel = 35)
@Composable
fun ImagePreviewScreenPreview() {

    val x = LocalContext.current.assets.open("loto_grid.webp")
        .use { inputStream -> BitmapFactory.decodeStream(inputStream) }
    val y = LocalContext.current.assets.open("loto_grid3.jpg")
        .use { inputStream -> BitmapFactory.decodeStream(inputStream) }

    Surface {
        ImagePreviewScreen(
            x, y,
            {},
            {},
        )
    }
}
