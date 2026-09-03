package org.github.raspopov.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// Состояние темы
class ThemeState(
    private val isDarkDefault: Boolean = false
) {
    var isDark by mutableStateOf(isDarkDefault)
        private set

    fun toggleTheme() {
        isDark = !isDark
    }

    fun setTheme(isDark: Boolean) {
        this.isDark = isDark
    }
}

@Composable
fun rememberThemeState(
    isDarkDefault: Boolean = isSystemInDarkTheme()
): ThemeState {
    return remember { ThemeState(isDarkDefault) }
}

@Composable
fun AppTheme(
    themeState: ThemeState = rememberThemeState(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (themeState.isDark) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val customColors = customColors(themeState.isDark)

    // Сохраняем тему в LocalConfiguration для доступа в любом месте
    CompositionLocalProvider(
        LocalCustomColors provides customColors,
        LocalThemeState provides themeState
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            content = {
                content()
            }
        )
    }
}

// CompositionLocal для доступа к кастомным цветам
val LocalCustomColors = compositionLocalOf<CustomColors> {
    error("No custom colors provided")
}

val LocalThemeState = compositionLocalOf<ThemeState> {
    error("No theme state provided")
}