package ch.sebpiller.easy.lotto.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun SideMenu() {

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Toggle (Switch)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = true, onCheckedChange = { })
            Spacer(Modifier.width(8.dp))
            Text("Smart mode")
        }

        // Radio button
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = true, onClick = { })
            Spacer(Modifier.width(8.dp))
            Text("Option A")
        }

        Button(onClick = {
//                                    captureAndSavePhoto(context, controller, executor) { result ->
//                                        lastSavedUri = result
//                                        val msg = if (result != null) {
//                                            "Saved: $result"
//                                        } else "Capture failed"
//
//                                        scope.launch {
//                                            snackbarHostState.showSnackbar(msg)
//                                        }
//
//                                        onClose()
//                                    }
        }) {
            Text("Scan grid")
        }
    }
}