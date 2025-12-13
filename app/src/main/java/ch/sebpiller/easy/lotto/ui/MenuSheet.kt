package ch.sebpiller.easy.lotto.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
private fun MenuSheet(modifier: Modifier = Modifier, onClose: () -> Unit, content: @Composable () -> Unit) {
    BoxWithConstraints(modifier) {
        val sheetWidth = maxWidth * 0.8f // 80% on phones; you can branch by windowSizeClass
        Surface(
            modifier = Modifier
                .width(sheetWidth)
                .fillMaxHeight(0.9f)
        ) {
            content()
        }
    }
}