package org.github.raspopov.theme

// theme/Theme.kt
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF424242),
    error = Color(0xFFD32F2F),
    onError = Color.White,
)

// Цвета для темной темы
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0B0B0),
    error = Color(0xFFEF5350),
    onError = Color.Black,
)

// Расширенные цвета для кастомных компонентов
data class CustomColors(
    val cardBackground: Color,
    val divider: Color,
    val iconTint: Color,
    val inputBackground: Color,
    val tabBackground: Color,
    val tabSelected: Color,
    val tabUnselected: Color,
)

@Composable
fun customColors(isDark: Boolean): CustomColors {
    return if (isDark) {
        CustomColors(
            cardBackground = Color(0xFF2C2C2C),
            divider = Color(0xFF404040),
            iconTint = Color.White.copy(alpha = 0.7f),
            inputBackground = Color(0xFF333333),
            tabBackground = Color(0xFF1E1E1E),
            tabSelected = Color(0xFF64B5F6),
            tabUnselected = Color(0xFF888888),
        )
    } else {
        CustomColors(
            cardBackground = Color.White,
            divider = Color(0xFFE0E0E0),
            iconTint = Color(0xFF666666),
            inputBackground = Color(0xFFF5F5F5),
            tabBackground = Color(0xFFF5F5F5),
            tabSelected = Color(0xFF1976D2),
            tabUnselected = Color(0xFF888888),
        )
    }
}