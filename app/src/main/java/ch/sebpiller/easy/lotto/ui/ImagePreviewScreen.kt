package ch.sebpiller.easy.lotto.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.sebpiller.easy.lotto.model.LottoGrid
import ch.sebpiller.easy.lotto.ocr.ImagePreparator

@Composable
fun ImagePreviewScreen(
    croppedImage: Bitmap,
    detectedGrid: LottoGrid?,
    onConfirm: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Header
        Text(
            text = "Prévisualisation du résultat",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        // Images comparison
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Original image
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Original",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f/3),
                    shape = MaterialTheme.shapes.medium,
                    color = Color.DarkGray
                ) {
                    Image(
                        bitmap = ImagePreparator().preprocessImage(croppedImage).asImageBitmap(),
                        contentDescription = "Prepared image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }
            }

//            Spacer(modifier = Modifier.width(8.dp))
//
//            // Processed image
//            Column(
//                modifier = Modifier.weight(1f),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = "Traité",
//                    color = Color.White,
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    modifier = Modifier.padding(bottom = 8.dp)
//                )
//                Surface(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .aspectRatio(1f),
//                    shape = MaterialTheme.shapes.medium,
//                    color = Color.DarkGray
//                ) {
//                    Image(
//                        bitmap = processedBitmap.asImageBitmap(),
//                        contentDescription = "Image traitée",
//                        modifier = Modifier.fillMaxSize(),
//                        contentScale = ContentScale.Crop
//                    )
//                }
//            }
        }

        // Detection info
        if (detectedGrid != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFF2A2A2A),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Grille détectée",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "Numéros trouvés: ${detectedGrid.numbers.size}",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp),
                        color = Color.LightGray
                    )
                    
                    if (detectedGrid.numbers.isNotEmpty()) {
                        Text(
                            text = detectedGrid.numbers
                                .sortedBy { it.value }
                                .joinToString(", ") { it.value.toString() },
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp),
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFF2A2A2A),
                contentColor = Color.White
            ) {
                Text(
                    text = "⚠️ Aucune grille détectée",
                    fontSize = 14.sp,
                    color = Color(0xFFFF6B6B),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onRetry,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Reprendre")
            }
            
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                enabled = detectedGrid != null && detectedGrid.numbers.isNotEmpty()
            ) {
                Text("Confirmer")
            }
        }
    }
}
