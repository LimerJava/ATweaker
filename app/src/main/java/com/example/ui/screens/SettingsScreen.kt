package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AddToHomeScreen
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AccentColorPreset
import com.example.data.AppLanguage
import com.example.data.AppThemeMode
import com.example.data.UserSettings
import com.example.ui.strings.Strings
import com.example.ui.theme.paletteForAccent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    strings: Strings,
    bottomPadding: PaddingValues,
    onSelectTheme: (AppThemeMode) -> Unit,
    onSelectAccent: (AccentColorPreset) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onToggleDangerous: (Boolean) -> Unit,
    onPinTerminalShortcut: () -> Boolean,
    onOpenUrl: (String) -> Unit,
) {
    var shortcutFeedback by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 6.dp,
            bottom = bottomPadding.calculateBottomPadding() + 92.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Theme Selection (Dark, Light, AMOLED Black, Night Violet)
        item {
            SettingsCard(
                title = strings.themeSection,
                icon = Icons.Rounded.Palette,
            ) {
                val themes = listOf(
                    Triple(AppThemeMode.DARK, strings.themeDark, Icons.Rounded.DarkMode to Color(0xFF171D29)),
                    Triple(AppThemeMode.LIGHT, strings.themeLight, Icons.Rounded.LightMode to Color(0xFFF4F6FB)),
                    Triple(AppThemeMode.AMOLED_BLACK, strings.themeBlack, Icons.Rounded.Smartphone to Color(0xFF000000)),
                    Triple(AppThemeMode.NIGHT_VIOLET, strings.themeNightViolet, Icons.Rounded.NightsStay to Color(0xFF1E1138)),
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    themes.forEach { (mode, label, iconAndColor) ->
                        val selected = settings.themeMode == mode
                        ThemeChipItem(
                            label = label,
                            icon = iconAndColor.first,
                            previewColor = iconAndColor.second,
                            selected = selected,
                            onClick = { onSelectTheme(mode) },
                            testTag = "theme_${mode.name.lowercase()}",
                        )
                    }
                }
            }
        }

        // Accent Color Swatches
        item {
            SettingsCard(
                title = strings.accentSection,
                icon = Icons.Rounded.Palette,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AccentColorPreset.entries.forEach { preset ->
                        val selected = settings.accentPreset == preset
                        val color = if (preset == AccentColorPreset.DYNAMIC) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            paletteForAccent(preset).swatchColor
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectAccent(preset) }
                                .padding(4.dp)
                                .testTag("accent_${preset.name.lowercase()}"),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selected) 3.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.25f),
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = preset.name,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = when (preset) {
                                    AccentColorPreset.DYNAMIC -> "You"
                                    AccentColorPreset.SAMSUNG_COBALT -> "Cobalt"
                                    AccentColorPreset.ELECTRIC_CYAN -> "Cyan"
                                    AccentColorPreset.NEON_VIOLET -> "Violet"
                                    AccentColorPreset.EMERALD_MINT -> "Mint"
                                    AccentColorPreset.CRIMSON_CORAL -> "Coral"
                                    AccentColorPreset.AMBER_GOLD -> "Amber"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // Language Toggle (Russian / English)
        item {
            SettingsCard(
                title = strings.languageSection,
                icon = Icons.Rounded.Language,
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = settings.language == AppLanguage.RU,
                        onClick = { onSelectLanguage(AppLanguage.RU) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("lang_ru_btn"),
                    ) {
                        Text("Русский")
                    }
                    SegmentedButton(
                        selected = settings.language == AppLanguage.EN,
                        onClick = { onSelectLanguage(AppLanguage.EN) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("lang_en_btn"),
                    ) {
                        Text("English")
                    }
                }
            }
        }

        // Add Terminal Shortcut to Home Screen
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable {
                        val ok = onPinTerminalShortcut()
                        shortcutFeedback = if (ok) strings.shortcutRequestedMsg else "Launcher pin unsupported"
                    }
                    .minimumInteractiveComponentSize()
                    .testTag("add_terminal_shortcut_btn"),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddToHomeScreen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(10.dp),
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.addTerminalShortcutTitle,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = shortcutFeedback ?: strings.addTerminalShortcutSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (shortcutFeedback != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }

        // Show Dangerous Tweaks Toggle
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                shape = RoundedCornerShape(22.dp),
                color = if (settings.showDangerousTweaks) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.28f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
                border = BorderStroke(
                    1.dp,
                    if (settings.showDangerousTweaks) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    },
                ),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp),
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.dangerousTweaksTitle,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = strings.dangerousTweaksSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = settings.showDangerousTweaks,
                        onCheckedChange = onToggleDangerous,
                        modifier = Modifier.testTag("toggle_dangerous_tweaks"),
                    )
                }
            }
        }

        // Community & Project Links (including https://t.me/Limex_basement)
        item {
            SettingsCard(
                title = strings.linksSection,
                icon = Icons.Rounded.Send,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LinkRowCard(
                        title = strings.telegramLinkTitle,
                        subtitle = strings.telegramLinkSubtitle,
                        icon = Icons.Rounded.Send,
                        onClick = { onOpenUrl("https://t.me/Limex_basement") },
                        testTag = "link_telegram_limex",
                    )
                    LinkRowCard(
                        title = strings.developerLinkTitle,
                        subtitle = strings.developerLinkSubtitle,
                        icon = Icons.Rounded.Code,
                        onClick = { onOpenUrl("https://github.com/RikkaApps/Shizuku") },
                        testTag = "link_developer_project",
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 680.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ThemeChipItem(
    label: String,
    icon: ImageVector,
    previewColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(previewColor)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            )
        },
        trailingIcon = {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        },
        label = { Text(label) },
        modifier = Modifier.testTag(testTag),
    )
}

@Composable
private fun LinkRowCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
