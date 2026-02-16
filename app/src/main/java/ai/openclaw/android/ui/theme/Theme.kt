package ai.openclaw.android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// OpenClaw 브랜드 컬러
val OpenClawOrange = Color(0xFFFF6B35)
val OpenClawOrangeDark = Color(0xFFE55A2B)
val OpenClawLobster = Color(0xFFFF4500)

private val DarkColorScheme = darkColorScheme(
    primary = OpenClawOrange,
    onPrimary = Color.White,
    primaryContainer = OpenClawOrangeDark,
    secondary = Color(0xFF4ECDC4),
    tertiary = Color(0xFFFF9F1C),
    background = Color(0xFF1A1A2E),
    surface = Color(0xFF16213E),
    error = Color(0xFFFF6B6B)
)

private val LightColorScheme = lightColorScheme(
    primary = OpenClawOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE5DC),
    secondary = Color(0xFF4ECDC4),
    tertiary = Color(0xFFFF9F1C),
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    error = Color(0xFFE53935)
)

@Composable
fun OpenClawTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
