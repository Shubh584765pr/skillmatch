package `in`.binkgo.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// SkillMatch brand: deep teal. Secondary roles (selected chips, bottom bar) use the same family,
// otherwise Material falls back to its default purple.
private val Teal = Color(0xFF0F766E)
private val TealLight = Color(0xFF5EEAD4)
private val TealPale = Color(0xFFCCFBF1)
private val TealDeep = Color(0xFF042F2E)

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = TealPale,
    onPrimaryContainer = TealDeep,
    secondary = Teal,
    secondaryContainer = TealPale,
    onSecondaryContainer = TealDeep,
    background = Color(0xFFF7F8FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFEEF1F4),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFF1F5F5), // dialogs, menus
    surfaceContainerHighest = Color(0xFFE8EEEE),
)

private val DarkColors = darkColorScheme(
    primary = TealLight,
    onPrimary = TealDeep,
    primaryContainer = Color(0xFF115E59),
    onPrimaryContainer = TealPale,
    secondary = TealLight,
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = TealPale,
)

/** Colours for the fit badge and match/gap markers, readable in light and dark mode. */
object FitColors {
    val excellentBg = Color(0xFFDCFCE7)
    val excellentFg = Color(0xFF166534)
    val goodBg = Color(0xFFFEF9C3)
    val goodFg = Color(0xFF854D0E)
    val stretchBg = Color(0xFFE5E7EB)
    val stretchFg = Color(0xFF374151)
    val match = Color(0xFF16A34A)
    val gap = Color(0xFFDC2626)
}

@Composable
fun SkillMatchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
