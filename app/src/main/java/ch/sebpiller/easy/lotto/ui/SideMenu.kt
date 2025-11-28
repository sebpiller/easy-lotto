package ch.sebpiller.easy.lotto.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.sebpiller.easy.lotto.ocr.LottoGridRecognizer
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch


@Composable
fun SideMenu(onClose: () -> Unit) {
    val scope = rememberCoroutineScope()

    Column {
        var showScanner by remember { mutableStateOf(false) }

        // Toggle (Switch)
        Row {
            Switch(checked = true, onCheckedChange = { })
            Spacer(Modifier.width(8.dp))
            Text("Smart mode")
        }

        // Radio button
        Row {
            RadioButton(
                selected = true, onClick = { })
            Spacer(Modifier.width(8.dp))
            Text("Option A")
        }

        TextButton(
            onClick = {
                showScanner = true
            }
        ) {
            Text("Scan grid")
        }

        TextButton(onClick = { onClose() }) {
            Text("Close")
        }

        if (showScanner) {
            TakePhotoCropperDialog(
                onDismissRequest = { showScanner = false },
                onResult = { bmp ->
                    scope.launch {
                        val img = InputImage.fromBitmap(bmp, 0)
                        val numbers = LottoGridRecognizer().extractAllNumbers(img)
                        showScanner = false
                    }
                }
            )

        }
    }
}