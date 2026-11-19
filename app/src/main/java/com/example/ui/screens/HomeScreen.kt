package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.DeviceInfo
import com.example.model.TweakCategory
import com.example.model.TweakExecutionLog
import com.example.model.TweakItem
import com.example.model.TweakOption
import com.example.ui.components.TweakCard
import com.example.ui.strings.Strings
import com.example.ui.theme.JetBrainsMonoFamily

@Composable
fun HomeScreen(
    deviceInfo: DeviceInfo,
    tweaks: List<TweakItem>,
    selectedCategory: TweakCategory?,
    tweakValues: Map<String, String>,
    lastLog: TweakExecutionLog?,
    showDangerous: Boolean,
    isRu: Boolean,
    strings: Strings,
    bottomPadding: PaddingValues,
    onSelectCategory: (TweakCategory?) -> Unit,
    onRefreshStates: () -> Unit,
    onToggleTweak: (TweakItem, Boolean) -> Unit,
    onSelectOption: (TweakItem, TweakOption) -> Unit,
    onRunAction: (TweakItem) -> Unit,
) {
    val visibleCategories = TweakCategory.entries.filter {
        showDangerous || it != TweakCategory.DANGEROUS
    }

    val filteredTweaks = if (selectedCategory == null) {
        tweaks
    } else {
        tweaks.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 4.dp,
            bottom = bottomPadding.calculateBottomPadding() + 92.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Architecture & matched count banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = String.format(strings.tweaksMatchedFormat, tweaks.size),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${deviceInfo.activeProfile.socVendor.name} • ${deviceInfo.activeProfile.displayType.name} • ${deviceInfo.activeProfile.kernelBranch}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    IconButton(
                        onClick = onRefreshStates,
                        modifier = Modifier.testTag("refresh_tweaks_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        // Last execution log card if present
        if (lastLog != null) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (lastLog.success) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (lastLog.success) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                        },
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (lastLog.success) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = if (lastLog.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${lastLog.title} (${lastLog.privilegeMode})",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = lastLog.output,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }

        // Category chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onSelectCategory(null) },
                    label = { Text("${strings.allCategories} (${tweaks.size})") },
                    modifier = Modifier.testTag("category_chip_all"),
                )
                visibleCategories.forEach { cat ->
                    val count = tweaks.count { it.category == cat }
                    if (count > 0) {
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { onSelectCategory(if (selectedCategory == cat) null else cat) },
                            label = { Text("${strings.categoryLabel(cat, isRu)} ($count)") },
                            modifier = Modifier.testTag("category_chip_${cat.name.lowercase()}"),
                        )
                    }
                }
            }
        }

        items(filteredTweaks, key = { it.id }) { tweak ->
            TweakCard(
                tweak = tweak,
                currentValue = tweakValues[tweak.id],
                isRu = isRu,
                strings = strings,
                onToggle = { enable -> onToggleTweak(tweak, enable) },
                onSelectOption = { opt -> onSelectOption(tweak, opt) },
                onRunAction = { onRunAction(tweak) },
                modifier = Modifier.widthIn(max = 680.dp),
            )
        }
    }
}
