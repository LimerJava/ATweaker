package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.data.AccentColorPreset

data class AccentPalette(
    val primaryDark: Color,
    val onPrimaryDark: Color,
    val primaryContainerDark: Color,
    val onPrimaryContainerDark: Color,
    val secondaryDark: Color,
    val primaryLight: Color,
    val onPrimaryLight: Color,
    val primaryContainerLight: Color,
    val onPrimaryContainerLight: Color,
    val secondaryLight: Color,
    val swatchColor: Color,
)

fun paletteForAccent(preset: AccentColorPreset): AccentPalette = when (preset) {
    AccentColorPreset.DYNAMIC,
    AccentColorPreset.ELECTRIC_CYAN -> AccentPalette(
        primaryDark = Color(0xFF4FD8EB),
        onPrimaryDark = Color(0xFF00363D),
        primaryContainerDark = Color(0xFF004F58),
        onPrimaryContainerDark = Color(0xFF97F0FF),
        secondaryDark = Color(0xFFB1CBD0),
        primaryLight = Color(0xFF006874),
        onPrimaryLight = Color(0xFFFFFFFF),
        primaryContainerLight = Color(0xFF97F0FF),
        onPrimaryContainerLight = Color(0xFF001F24),
        secondaryLight = Color(0xFF4A6267),
        swatchColor = Color(0xFF22D3EE),
    )

    AccentColorPreset.SAMSUNG_COBALT -> AccentPalette(
        primaryDark = Color(0xFF8AB4F8),
        onPrimaryDark = Color(0xFF002E69),
        primaryContainerDark = Color(0xFF0842A0),
        onPrimaryContainerDark = Color(0xFFD3E3FD),
        secondaryDark = Color(0xFFA8C7FA),
        primaryLight = Color(0xFF1A73E8),
        onPrimaryLight = Color(0xFFFFFFFF),
        primaryContainerLight = Color(0xFFD3E3FD),
        onPrimaryContainerLight = Color(0xFF041E49),
        secondaryLight = Color(0xFF4C5D77),
        swatchColor = Color(0xFF2962FF),
    )

    AccentColorPreset.NEON_VIOLET -> AccentPalette(
        primaryDark = Color(0xFFCBB2FF),
        onPrimaryDark = Color(0xFF33147A),
        primaryContainerDark = Color(0xFF4B2C94),
        onPrimaryContainerDark = Color(0xFFE9DDFF),
        secondaryDark = Color(0xFFD0BCFF),
        primaryLight = Color(0xFF6750A4),
        onPrimaryLight = Color(0xFFFFFFFF),
        primaryContainerLight = Color(0xFFEADDFF),
        onPrimaryContainerLight = Color(0xFF21005D),
        secondaryLight = Color(0xFF625B71),
        swatchColor = Color(0xFF9D4EDD),
    )

    AccentColorPreset.EMERALD_MINT -> AccentPalette(
        primaryDark = Color(0xFF5EE8A7),
        onPrimaryDark = Color(0xFF003821),
        primaryContainerDark = Color(0xFF005232),
        onPrimaryContainerDark = Color(0xFF7EFFC0),
        secondaryDark = Color(0xFFB5CCB9),
        primaryLight = Color(0xFF006C44),
        onPrimaryLight = Color(0xFFFFFFFF),
        primaryContainerLight = Color(0xFF7EFFC0),
        onPrimaryContainerLight = Color(0xFF002111),
        secondaryLight = Color(0xFF4E6354),
        swatchColor = Color(0xFF10B981),
    )

    AccentColorPreset.CRIMSON_CORAL -> AccentPalette(
        primaryDark = Color(0xFFFFB3B4),
        onPrimaryDark = Color(0xFF680015),
        primaryContainerDark = Color(0xFF930022),
        onPrimaryContainerDark = Color(0xFFFFDAD9),
        secondaryDark = Color(0xFFE6BDBF),
        primaryLight = Color(0xFFBA1A3A),
        onPrimaryLight = Color(0xFFFFFFFF),
        primaryContainerLight = Color(0xFFFFDAD9),
        onPrimaryContainerLight = Color(0xFF41000B),
        secondaryLight = Color(0xFF775658),
        swatchColor = Color(0xFFF43F5E),
    )

    AccentColorPreset.AMBER_GOLD -> AccentPalette(
        primaryDark = Color(0xFFFFCA62),
        onPrimaryDark = Color(0xFF422C00),
        primaryContainerDark = Color(0xFF5F4100),
        onPrimaryContainerDark = Color(0xFFFFDEA6),
        secondaryDark = Color(0xFFD8C4A0),
        primaryLight = Color(0xFF7E5700),
        onPrimaryLight = Color(0xFFFFFFFF),
        primaryContainerLight = Color(0xFFFFDEA6),
        onPrimaryContainerLight = Color(0xFF281900),
        secondaryLight = Color(0xFF6D5C3F),
        swatchColor = Color(0xFFF59E0B),
    )
}
