package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.DeviceInfo
import com.example.data.GalaxyAModelProfile
import com.example.data.SamsungDeviceCatalog
import com.example.data.UpdateCheckResult
import com.example.shizuku.ShizukuState
import com.example.shizuku.ShizukuStatus
import com.example.ui.MainTab
import com.example.ui.strings.Strings

@Composable
fun DeviceStatusHeaderCard(
    deviceInfo: DeviceInfo,
    updateResult: UpdateCheckResult?,
    isCheckingUpdate: Boolean,
    shizukuState: ShizukuState,
    strings: Strings,
    onShizukuClick: () -> Unit,
    onModelClick: () -> Unit,
    onUpdateBadgeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = deviceInfo.activeProfile
    val statusColor by animateColorAsState(
        targetValue = when (shizukuState.status) {
            ShizukuStatus.CONNECTED_ADB,
            ShizukuStatus.CONNECTED_ROOT -> Color(0xFF22C55E)
            ShizukuStatus.PERMISSION_REQUIRED,
            ShizukuStatus.CONNECTING -> Color(0xFFF59E0B)
            ShizukuStatus.DISCONNECTED,
            ShizukuStatus.NOT_INSTALLED -> if (shizukuState.hasLocalRoot) Color(0xFF22C55E) else Color(0xFFEF4444)
        },
        label = "shizukuColor",
    )

    val shizukuLabel = when (shizukuState.status) {
        ShizukuStatus.CONNECTED_ROOT -> strings.shizukuRootConnected
        ShizukuStatus.CONNECTED_ADB -> strings.shizukuAdbConnected
        ShizukuStatus.PERMISSION_REQUIRED -> strings.shizukuPermissionNeeded
        ShizukuStatus.CONNECTING -> strings.shizukuConnecting
        ShizukuStatus.DISCONNECTED -> if (shizukuState.hasLocalRoot) "Root (su)" else strings.shizukuDisconnected
        ShizukuStatus.NOT_INSTALLED -> if (shizukuState.hasLocalRoot) "Root (su)" else strings.shizukuNotInstalled
    }

    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulse.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseAlpha",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 4.dp,
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                ),
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Left: Phone model + Android version directly below
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onModelClick)
                            .padding(end = 8.dp, top = 2.dp, bottom = 2.dp)
                            .testTag("device_model_header"),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(40.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.PhoneAndroid,
                                    contentDescription = profile.marketingName,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.marketingName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Rounded.SwapHoriz,
                                    contentDescription = strings.modelSelectorTitle,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Text(
                                text = buildString {
                                    append("Android ${deviceInfo.androidVersion}")
                                    val ui = deviceInfo.oneUiVersion ?: profile.latestOneUiVersion
                                    append(" • $ui")
                                    append(" • ${profile.socName}")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    // Right: Shizuku ADB status indicator pill
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = statusColor.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onShizukuClick)
                            .minimumInteractiveComponentSize()
                            .testTag("shizuku_status_pill"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .alpha(pulseAlpha)
                                    .clip(CircleShape)
                                    .background(statusColor),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.Usb,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = shizukuLabel,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = statusColor,
                                maxLines = 1,
                            )
                        }
                    }
                }

                // Bottom row inside top banner: Update available pill (if update exists) + model code / arch chip
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (updateResult?.isUpdateAvailable == true) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onUpdateBadgeClick)
                                .testTag("update_available_badge"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SystemUpdate,
                                    contentDescription = strings.updateAvailable,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "${strings.updateAvailable}: ${updateResult.availableLabel ?: ""}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onUpdateBadgeClick),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isCheckingUpdate) strings.checkingUpdate else strings.upToDate,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Text(
                            text = "${deviceInfo.rawModel} • ${deviceInfo.arch} • ${profile.maxRefreshRateHz}Hz",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OvalFloatingBottomBar(
    selectedTab: MainTab,
    strings: Strings,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        Triple(MainTab.HOME, strings.tabHome, Icons.Filled.Home to Icons.Outlined.Home),
        Triple(MainTab.SEARCH, strings.tabSearch, Icons.Filled.Search to Icons.Outlined.Search),
        Triple(MainTab.SETTINGS, strings.tabSettings, Icons.Filled.Settings to Icons.Outlined.Settings),
        Triple(MainTab.TERMINAL, strings.tabTerminal, Icons.Filled.Terminal to Icons.Outlined.Terminal),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
            tonalElevation = 8.dp,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { (tab, label, icons) ->
                    val selected = tab == selectedTab
                    val pillColor by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        },
                        label = "tabPillColor",
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        label = "tabContentColor",
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(pillColor)
                            .clickable { onSelectTab(tab) }
                            .minimumInteractiveComponentSize()
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag("nav_tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = if (selected) icons.first else icons.second,
                                contentDescription = label,
                                tint = contentColor,
                                modifier = Modifier.size(21.dp),
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = contentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModelSelectorDialog(
    deviceInfo: DeviceInfo,
    strings: Strings,
    onSelectProfile: (GalaxyAModelProfile?) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(strings.modelSelectorTitle, style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = strings.closeBtn)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (!deviceInfo.isProfileOverridden) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onSelectProfile(null)
                                onDismiss()
                            },
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = strings.modelAutoDetected,
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = "${deviceInfo.manufacturer} ${deviceInfo.rawModel} (${deviceInfo.socHardware})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                items(SamsungDeviceCatalog.profiles, key = { it.id }) { profile ->
                    val isSelected = deviceInfo.isProfileOverridden && deviceInfo.activeProfile.id == profile.id
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onSelectProfile(profile)
                                onDismiss()
                            },
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${profile.marketingName} (${profile.modelPrefixes.first()})",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = "${profile.socName} • ${profile.maxRefreshRateHz}Hz • ${profile.displayType.name} • ${profile.kernelBranch}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.closeBtn)
            }
        },
    )
}

@Composable
fun ShizukuConnectionDialog(
    shizukuState: ShizukuState,
    strings: Strings,
    onRequestPermission: () -> Unit,
    onOpenShizuku: () -> Unit,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.shizukuDialogTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strings.shizukuDialogBody,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Status: ${shizukuState.status.name}",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        shizukuState.shizukuVersion?.let {
                            Text("Shizuku API v$it", style = MaterialTheme.typography.bodySmall)
                        }
                        shizukuState.uid?.let {
                            Text("Remote UID: $it", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onRequestPermission()
                onRefresh()
            }) {
                Text(strings.shizukuRequestBtn)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = onOpenShizuku) {
                    Text(strings.shizukuOpenAppBtn)
                }
                TextButton(onClick = onDismiss) {
                    Text(strings.closeBtn)
                }
            }
        },
    )
}

@Composable
fun FotaDetailsDialog(
    deviceInfo: DeviceInfo,
    updateResult: UpdateCheckResult?,
    strings: Strings,
    onRecheck: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.fotaDialogTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${deviceInfo.activeProfile.marketingName} (${deviceInfo.rawModel})",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "CSC: ${deviceInfo.cscCode} • Build: ${deviceInfo.buildIncremental}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Kernel: ${deviceInfo.kernelVersion}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (updateResult != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (updateResult.isUpdateAvailable) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (updateResult.isUpdateAvailable) strings.updateAvailable else strings.upToDate,
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = "${updateResult.currentLabel} -> ${updateResult.availableLabel ?: "Latest"}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = "Source: ${updateResult.source}",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onRecheck) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Check")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.closeBtn)
            }
        },
    )
}
