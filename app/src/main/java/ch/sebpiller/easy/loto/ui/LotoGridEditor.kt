package ch.sebpiller.easy.loto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.sebpiller.easy.loto.domain.LotoGrid
import ch.sebpiller.easy.loto.domain.LotoNum
import ch.sebpiller.easy.loto.domain.XY
import ch.sebpiller.easy.loto.ui.samples.LotoGridSamples

@Composable
fun LotoGridEditor(
    initialGrid: LotoGrid? = null,
    onGridChanged: (LotoGrid?) -> Unit = {}
) {
    // Store the current state of cells as a map of position to value
    var cellValues by remember {
        mutableStateOf<Map<Pair<Int, Int>, Int>>(
            initialGrid?.numbers?.associate { num ->
                (num.position.row to num.position.col) to num.value
            } ?: emptyMap()
        )
    }

    val positions = (0..2).flatMap { r -> (0..8).map { c -> r to c } }

    Column(modifier = Modifier.fillMaxWidth()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(9),
            modifier = Modifier.widthIn(min = 100.dp, max = 600.dp)
        ) {
            items(positions) { (row, col) ->
                val currentValue = cellValues[row to col]

                EditableCell(
                    value = currentValue,
                    row = row,
                    col = col,
                    onValueChange = { newValue ->
                        cellValues = if (newValue == null) {
                            cellValues - (row to col)
                        } else {
                            cellValues + ((row to col) to newValue)
                        }

                        // Try to construct a LotoGrid from current values
                        val grid = try {
                            val numbers = cellValues.map { (pos, value) ->
                                LotoNum(value, XY(col = pos.second, row = pos.first))
                            }
                            LotoGrid(numbers)
                        } catch (e: Exception) {
                            null
                        }
                        onGridChanged(grid)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                cellValues = emptyMap()
                onGridChanged(null)
            }) {
                Text("Clear All")
            }

            Button(onClick = {
                cellValues = initialGrid?.numbers?.associate { num ->
                    (num.position.row to num.position.col) to num.value
                } ?: emptyMap()
                onGridChanged(initialGrid)
            }) {
                Text("Reset")
            }

            Button(
                onClick = {
                    try {
                        val numbers = cellValues.map { (pos, value) ->
                            LotoNum(value, XY(col = pos.second, row = pos.first))
                        }
                        val grid = LotoGrid(numbers).asValidGrid()
                        onGridChanged(grid)
                    } catch (e: Exception) {
                        // Invalid grid, show error or just ignore
                    }
                },
                enabled = cellValues.size == 15
            ) {
                Text("Validate")
            }
        }
    }
}

@Composable
private fun EditableCell(
    value: Int?,
    row: Int,
    col: Int,
    onValueChange: (Int?) -> Unit
) {
    var textValue by remember(value) { mutableStateOf(value?.toString() ?: "") }
    var isEditing by remember { mutableStateOf(false) }

    val backgroundColor = Color.Blue.copy(alpha = .3f)
    val borderColor = Color.Black

    Card(
        modifier = Modifier
            .aspectRatio(0.85f)
            .padding(1.dp)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable { isEditing = true },
        colors = CardDefaults.cardColors(
            containerColor = if (value != null) Color.LightGray else backgroundColor,
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isEditing) {
                BasicTextField(
                    value = textValue,
                    onValueChange = { newText ->
                        // Only allow numbers 1-90
                        if (newText.isEmpty()) {
                            textValue = ""
                            onValueChange(null)
                            isEditing = false
                        } else if (newText.all { it.isDigit() }) {
                            val num = newText.toIntOrNull()
                            if (num != null && num in 1..90) {
                                textValue = newText
                                onValueChange(num)
                                isEditing = false
                            } else if (newText.length <= 2) {
                                textValue = newText
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(4.dp),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true
                )
            } else {
                Text(
                    text = value?.toString() ?: "",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun LotoGridEditorPreview() {
    Surface {
        LotoGridEditor(
            initialGrid = LotoGridSamples.SAMPLE1,
            onGridChanged = { grid ->
                println("Grid changed: ${grid?.numbers?.size ?: 0} numbers")
            }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun LotoGridEditorEmptyPreview() {
    Surface {
        LotoGridEditor(
            initialGrid = null,
            onGridChanged = { grid ->
                println("Grid changed: ${grid?.numbers?.size ?: 0} numbers")
            }
        )
    }
}
