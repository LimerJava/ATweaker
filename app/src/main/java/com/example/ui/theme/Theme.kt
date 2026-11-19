package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.AccentColorPreset
import com.example.data.AppThemeMode

@Composable
fun ATweakerTheme(
    themeMode: AppThemeMode,
    accentPreset: AccentColorPreset,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val palette = paletteForAccent(accentPreset)
    val useDynamic = accentPreset == AccentColorPreset.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val baseScheme = when (themeMode) {
        AppThemeMode.LIGHT -> {
            if (useDynamic) {
                dynamicLightColorScheme(context)
            } else {
                lightColorScheme(
                    primary = palette.primaryLight,
                    onPrimary = palette.onPrimaryLight,
                    primaryContainer = palette.primaryContainerLight,
                    onPrimaryContainer = palette.onPrimaryContainerLight,
                    secondary = palette.secondaryLight,
                    background = Color(0xFFF4F6FB),
                    surface = Color(0xFFF4F6FB),
                    surfaceContainer = Color(0xFFFFFFFF),
                    surfaceContainerHigh = Color(0xFFEBF0F8),
                    surfaceVariant = Color(0xFFE3E8F2),
                    onBackground = Color(0xFF11141B),
                    onSurface = Color(0xFF11141B),
                    onSurfaceVariant = Color(0xFF475063),
                    outlineVariant = Color(0xFFD0D7E4),
                )
            }
        }

        AppThemeMode.DARK -> {
            if (useDynamic) {
                dynamicDarkColorScheme(context)
            } else {
                darkColorScheme(
                    primary = palette.primaryDark,
                    onPrimary = palette.onPrimaryDark,
                    primaryContainer = palette.primaryContainerDark,
                    onPrimaryContainer = palette.onPrimaryContainerDark,
                    secondary = palette.secondaryDark,
                    background = Color(0xFF0E121A),
                    surface = Color(0xFF0E121A),
                    surfaceContainer = Color(0xFF171D29),
                    surfaceContainerHigh = Color(0xFF1F2736),
                    surfaceVariant = Color(0xFF232B3B),
                    onBackground = Color(0xFFEAF0FA),
                    onSurface = Color(0xFFEAF0FA),
                    onSurfaceVariant = Color(0xFF9AA7BD),
                    outlineVariant = Color(0xFF2E384B),
                )
            }
        }

        AppThemeMode.AMOLED_BLACK -> {
            val dynamicBase = if (useDynamic) dynamicDarkColorScheme(context) else null
            darkColorScheme(
                primary = dynamicBase?.primary ?: palette.primaryDark,
                onPrimary = dynamicBase?.onPrimary ?: palette.onPrimaryDark,
                primaryContainer = dynamicBase?.primaryContainer ?: palette.primaryContainerDark,
                onPrimaryContainer = dynamicBase?.onPrimaryContainer ?: palette.onPrimaryContainerDark,
                secondary = dynamicBase?.secondary ?: palette.secondaryDark,
                background = Color(0xFF000000),
                surface = Color(0xFF000000),
                surfaceContainer = Color(0xFF0A0A0D),
                surfaceContainerHigh = Color(0xFF121218),
                surfaceVariant = Color(0xFF16161D),
                onBackground = Color(0xFFF2F4F8),
                onSurface = Color(0xFFF2F4F8),
                onSurfaceVariant = Color(0xFF9BA3B4),
                outlineVariant = Color(0xFF23232E),
            )
        }

        AppThemeMode.NIGHT_VIOLET -> {
            val dynamicBase = if (useDynamic) dynamicDarkColorScheme(context) else null
            darkColorScheme(
                primary = dynamicBase?.primary ?: Color(0xFFCBB2FF).takeIf { accentPreset == AccentColorPreset.DYNAMIC } ?: palette.primaryDark,
                onPrimary = dynamicBase?.onPrimary ?: palette.onPrimaryDark,
                primaryContainer = dynamicBase?.primaryContainer ?: Color(0xFF3F2378),
                onPrimaryContainer = dynamicBase?.onPrimaryContainer ?: Color(0xFFEDE4FF),
                secondary = dynamicBase?.secondary ?: Color(0xFFD4BBFF),
                background = Color(0xFF0B0714),
                surface = Color(0xFF0B0714),
                surfaceContainer = Color(0xFF150E26),
                surfaceContainerHigh = Color(0xFF1E1636),
                surfaceVariant = Color(0xFF261D42),
                onBackground = Color(0xFFF0E9FF),
                onSurface = Color(0xFFF0E9FF),
                onSurfaceVariant = Color(0xFFAFA2CC),
                outlineVariant = Color(0xFF332757),
            )
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val isLight = themeMode == AppThemeMode.LIGHT
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = isLight
                    isAppearanceLightNavigationBars = isLight
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = baseScheme,
        typography = AppTypography,
        content = content,
    )
}
