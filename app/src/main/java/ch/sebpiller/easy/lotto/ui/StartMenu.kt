package ch.sebpiller.easy.lotto.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartMenu(function: () -> Unit) {

    val mode = remember { mutableStateOf("none") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = {
            Column(modifier = Modifier.padding(it)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxHeight(0.5f)
                ) {
                    Button(
                        modifier = Modifier.fillMaxSize(),
                        onClick = {
                            mode.value = "classical"
                            function()
                        },
                        content = { Text("Classical", autoSize = TextAutoSize.StepBased()) }
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxHeight()
                )
                {
                    Button(
                        modifier = Modifier.fillMaxSize(),
                        enabled = false,
                        onClick = {
                            println("CLICKED")
                            mode.value = "expert"
                            function()
                        },
                        content = { Text("Expert", autoSize = TextAutoSize.StepBased()) }
                    )
                }
            }
        }
    )
}

@Composable
@Preview
fun StartMenuPreview() {
    Surface {
        StartMenu {
        }
    }
}

