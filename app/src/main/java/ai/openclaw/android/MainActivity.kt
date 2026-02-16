package ai.openclaw.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ai.openclaw.android.ui.MainScreen
import ai.openclaw.android.ui.SettingsScreen
import ai.openclaw.android.ui.theme.OpenClawTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenClawTheme {
                var showSettings by remember { mutableStateOf(false) }
                
                if (showSettings) {
                    SettingsScreen(
                        onNavigateBack = { showSettings = false }
                    )
                } else {
                    MainScreen(
                        onNavigateToSettings = { showSettings = true }
                    )
                }
            }
        }
    }
}
