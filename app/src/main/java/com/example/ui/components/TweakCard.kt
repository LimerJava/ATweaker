package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DisplaySettings
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.TweakCategory
import com.example.model.TweakControlType
import com.example.model.TweakItem
import com.example.model.TweakOption
import com.example.ui.strings.Strings
import com.example.ui.theme.JetBrainsMonoFamily

@Composable
fun TweakCard(
    tweak: TweakItem,
    currentValue: String?,
    isRu: Boolean,
    strings: Strings,
    matchedSynonyms: List<String> = emptyList(),
    onToggle: (Boolean) -> Unit,
    onSelectOption: (TweakOption) -> Unit,
    onRunAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTechnical by remember { mutableStateOf(false) }

    val categoryIcon = when (tweak.category) {
        TweakCategory.DISPLAY -> Icons.Rounded.DisplaySettings
        TweakCategory.PERFORMANCE -> Icons.Rounded.Speed
        TweakCategory.BATTERY -> Icons.Rounded.BatteryChargingFull
        TweakCategory.ONE_UI -> Icons.Rounded.Tune
        TweakCategory.NETWORK -> Icons.Rounded.Wifi
        TweakCategory.CAMERA_AUDIO -> Icons.Rounded.GraphicEq
        TweakCategory.DANGEROUS -> Icons.Rounded.WarningAmber
    }

    val isEnabled = if (tweak.expectedEnabledValue != null) {
        currentValue == tweak.expectedEnabledValue
    } else {
        currentValue == "1" || currentValue == "enabled" || currentValue == "true"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tweak_card_${tweak.id}"),
        shape = RoundedCornerShape(22.dp),
        color = if (tweak.isDangerous) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.22f)
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (tweak.isDangerous) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            },
        ),
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (tweak.isDangerous) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        },
                        modifier = Modifier.size(38.dp),
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = if (tweak.isDangerous) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = strings.categoryLabel(tweak.category, isRu),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (tweak.isDangerous) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                            )
                            if (tweak.isDangerous) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.error,
                                ) {
                                    Text(
                                        text = strings.dangerousBadge,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isRu) tweak.titleRu else tweak.titleEn,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                if (tweak.controlType == TweakControlType.TOGGLE) {
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { onToggle(it) },
                        modifier = Modifier.testTag("tweak_switch_${tweak.id}"),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isRu) tweak.descriptionRu else tweak.descriptionEn,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (matchedSynonyms.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${strings.matchedBySynonym} ${matchedSynonyms.joinToString(", ")}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (tweak.controlType == TweakControlType.CHOICE && tweak.options.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    tweak.options.forEach { option ->
                        val selected = currentValue != null && (
                            currentValue == option.expectedReadValue ||
                                currentValue == option.id
                            )
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectOption(option) },
                            label = {
                                Text(
                                    text = if (isRu) option.labelRu else option.labelEn,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                            modifier = Modifier.testTag("tweak_option_${tweak.id}_${option.id}"),
                        )
                    }
                }
            }

            if (tweak.controlType == TweakControlType.ACTION) {
                Spacer(Modifier.height(10.dp))
                FilledTonalButton(
                    onClick = onRunAction,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("tweak_action_${tweak.id}"),
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.runActionBtn)
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showTechnical = !showTechnical }
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Code,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "ADB / ${strings.whyHiddenLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = if (showTechnical) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }

            AnimatedVisibility(visible = showTechnical) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = if (isRu) tweak.whyHiddenRu else tweak.whyHiddenEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        val cmdPreview = tweak.enableCommand
                            ?: tweak.actionCommand
                            ?: tweak.options.firstOrNull()?.command
                            ?: ""
                        if (cmdPreview.isNotBlank()) {
                            Text(
                                text = "$ $cmdPreview",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}
