package ch.sebpiller.easy.lotto

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ch.sebpiller.easy.lotto.ui.MainScreen
import ch.sebpiller.easy.lotto.ui.SideMenu
import ch.sebpiller.easy.lotto.ui.theme.EasyLottoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "onCreate called")
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)


        setContent {
            EasyLottoTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen(
                        menuContent = { onClose ->
                            SideMenu(onClose)
                        },
                        assets = assets,
                    )
                }
            }
        }
    }
}