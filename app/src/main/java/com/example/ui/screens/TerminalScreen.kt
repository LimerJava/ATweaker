package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.terminal.CuratedGitHubApp
import com.example.terminal.FileEntry
import com.example.terminal.GitHubReleaseAsset
import com.example.terminal.GitHubReleaseInfo
import com.example.terminal.InstalledPackage
import com.example.terminal.TerminalLine
import com.example.terminal.TerminalPackageCatalog
import com.example.ui.TerminalSubTab
import com.example.ui.strings.Strings
import com.example.ui.theme.JetBrainsMonoFamily

@Composable
fun TerminalScreen(
    subTab: TerminalSubTab,
    terminalLines: List<TerminalLine>,
    installedPackages: List<InstalledPackage>,
    commandHistory: List<String>,
    currentDir: String,
    githubRepoInput: String,
    githubLoading: Boolean,
    githubRelease: GitHubReleaseInfo?,
    githubMessage: String?,
    downloadProgress: Float?,
    curatedApps: List<CuratedGitHubApp>,
    fileBrowserPath: String,
    fileEntries: List<FileEntry>,
    fileAccessMode: String,
    filePreview: Pair<String, String>?,
    isRu: Boolean,
    strings: Strings,
    bottomPadding: PaddingValues,
    onSelectSubTab: (TerminalSubTab) -> Unit,
    onRunCommand: (String) -> Unit,
    onInstallPackage: (String) -> Unit,
    onRemovePackage: (String) -> Unit,
    onGithubInputChange: (String) -> Unit,
    onFetchGithubRelease: (String) -> Unit,
    onDownloadGithubAsset: (GitHubReleaseAsset) -> Unit,
    onLoadFileDir: (String) -> Unit,
    onNavigateUpDir: () -> Unit,
    onPreviewFile: (FileEntry) -> Unit,
    onDismissFilePreview: () -> Unit,
    onCreateDir: (String) -> Unit,
    onDeleteEntry: (FileEntry) -> Unit,
    onOpenPathInTerminal: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(
                start = 14.dp,
                end = 14.dp,
                top = 4.dp,
                bottom = bottomPadding.calculateBottomPadding() + 86.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp),
        ) {
            val subTabs = listOf(
                TerminalSubTab.SHELL to strings.terminalSubShell,
                TerminalSubTab.PACKAGES to strings.terminalSubPkg,
                TerminalSubTab.GITHUB to strings.terminalSubGithub,
                TerminalSubTab.FILES to strings.terminalSubFiles,
            )
            subTabs.forEachIndexed { idx, (tab, label) ->
                SegmentedButton(
                    selected = subTab == tab,
                    onClick = { onSelectSubTab(tab) },
                    shape = SegmentedButtonDefaults.itemShape(index = idx, count = subTabs.size),
                    modifier = Modifier.testTag("terminal_subtab_${tab.name.lowercase()}"),
                ) {
                    Text(
                        text = label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        when (subTab) {
            TerminalSubTab.SHELL -> ShellConsoleView(
                lines = terminalLines,
                history = commandHistory,
                currentDir = currentDir,
                strings = strings,
                onRunCommand = onRunCommand,
            )

            TerminalSubTab.PACKAGES -> PackageManagerView(
                installedPackages = installedPackages,
                isRu = isRu,
                strings = strings,
                onInstallPackage = onInstallPackage,
                onRemovePackage = onRemovePackage,
                onRunInShell = { cmd ->
                    onSelectSubTab(TerminalSubTab.SHELL)
                    onRunCommand(cmd)
                },
            )

            TerminalSubTab.GITHUB -> GitHubInstallerView(
                repoInput = githubRepoInput,
                loading = githubLoading,
                release = githubRelease,
                message = githubMessage,
                downloadProgress = downloadProgress,
                curatedApps = curatedApps,
                isRu = isRu,
                strings = strings,
                onInputChange = onGithubInputChange,
                onFetch = onFetchGithubRelease,
                onDownloadAsset = onDownloadGithubAsset,
            )

            TerminalSubTab.FILES -> RootFileManagerView(
                currentPath = fileBrowserPath,
                entries = fileEntries,
                accessMode = fileAccessMode,
                filePreview = filePreview,
                strings = strings,
                onLoadDir = onLoadFileDir,
                onNavigateUp = onNavigateUpDir,
                onPreviewFile = onPreviewFile,
                onDismissPreview = onDismissFilePreview,
                onCreateDir = onCreateDir,
                onDeleteEntry = onDeleteEntry,
                onOpenInTerminal = onOpenPathInTerminal,
            )
        }
    }
}

@Composable
private fun ShellConsoleView(
    lines: List<TerminalLine>,
    history: List<String>,
    currentDir: String,
    strings: Strings,
    onRunCommand: (String) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var historyIdx by remember { mutableStateOf(-1) }
    val listState = rememberLazyListState()

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp),
    ) {
        // Dark Termux terminal viewport
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0A0D12),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF121721))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Terminal,
                            contentDescription = null,
                            tint = Color(0xFF4ADE80),
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "termux-persistent • ${currentDir.substringAfterLast('/').ifBlank { "/" }}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = "clear",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                        color = Color(0xFF38BDF8),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onRunCommand("clear") }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(lines, key = { it.id }) { line ->
                        val color = when {
                            line.isError -> Color(0xFFF87171)
                            line.isCommand -> Color(0xFF4ADE80)
                            line.isAccent -> Color(0xFF38BDF8)
                            else -> Color(0xFFE2E8F0)
                        }
                        Text(
                            text = line.text,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.5.sp,
                                lineHeight = 15.5.sp,
                                fontWeight = if (line.isCommand) FontWeight.Bold else FontWeight.Normal,
                            ),
                            color = color,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Termux-style quick accessory bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val quickChips = listOf(
                "fastfetch",
                "pkg list",
                "htop",
                "saminfo",
                "debloat-audit",
                "ls -la",
                "getprop ro.build.version.oneui",
            )
            quickChips.forEach { cmd ->
                AssistChip(
                    onClick = { onRunCommand(cmd) },
                    label = {
                        Text(
                            cmd,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                        )
                    },
                )
            }
            if (history.isNotEmpty()) {
                AssistChip(
                    onClick = {
                        historyIdx = if (historyIdx < 0) history.lastIndex else (historyIdx - 1).coerceAtLeast(0)
                        input = history[historyIdx]
                    },
                    leadingIcon = { Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    label = { Text("↑") },
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // Command input bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("terminal_command_input"),
                placeholder = { Text(strings.terminalInputHint, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFamily),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (input.isNotBlank()) {
                            onRunCommand(input)
                            input = ""
                            historyIdx = -1
                        }
                    }
                ),
            )
            FilledTonalButton(
                onClick = {
                    if (input.isNotBlank()) {
                        onRunCommand(input)
                        input = ""
                        historyIdx = -1
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .height(54.dp)
                    .testTag("terminal_send_btn"),
            ) {
                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Run")
            }
        }
    }
}

@Composable
private fun PackageManagerView(
    installedPackages: List<InstalledPackage>,
    isRu: Boolean,
    strings: Strings,
    onInstallPackage: (String) -> Unit,
    onRemovePackage: (String) -> Unit,
    onRunInShell: (String) -> Unit,
) {
    val installedIds = installedPackages.map { it.id }.toSet()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = strings.pkgPersistenceBanner,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        items(TerminalPackageCatalog.availablePackages, key = { it.id }) { pkg ->
            val isInstalled = pkg.id in installedIds
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = pkg.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                            )
                            Text(
                                text = pkg.version,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = pkg.sizeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (isRu) pkg.descriptionRu else pkg.descriptionEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (isInstalled) {
                            Spacer(Modifier.height(6.dp))
                            AssistChip(
                                onClick = { onRunInShell(pkg.binaryCommands.first()) },
                                label = {
                                    Text(
                                        "$ ${pkg.binaryCommands.first()}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                    )
                                },
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    if (isInstalled) {
                        OutlinedButton(
                            onClick = { onRemovePackage(pkg.id) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("pkg_remove_${pkg.id}"),
                        ) {
                            Text(strings.pkgRemove)
                        }
                    } else {
                        Button(
                            onClick = { onInstallPackage(pkg.id) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("pkg_install_${pkg.id}"),
                        ) {
                            Text(strings.pkgInstall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GitHubInstallerView(
    repoInput: String,
    loading: Boolean,
    release: GitHubReleaseInfo?,
    message: String?,
    downloadProgress: Float?,
    curatedApps: List<CuratedGitHubApp>,
    isRu: Boolean,
    strings: Strings,
    onInputChange: (String) -> Unit,
    onFetch: (String) -> Unit,
    onDownloadAsset: (GitHubReleaseAsset) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = repoInput,
                onValueChange = onInputChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("github_repo_input"),
                placeholder = { Text(strings.githubInputHint) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
            Button(
                onClick = { onFetch(repoInput) },
                enabled = !loading && repoInput.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(54.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(strings.githubFetchRelease)
                }
            }
        }

        if (downloadProgress != null) {
            LinearProgressIndicator(
                progress = { downloadProgress },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (!message.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

        if (release != null) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "${release.repoFullName} • ${release.tagName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (release.body.isNotBlank()) {
                        Text(
                            text = release.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    release.assets.forEach { asset ->
                        val isApk = asset.name.endsWith(".apk", ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = if (isApk) Icons.Rounded.InstallMobile else Icons.Rounded.Archive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        asset.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        "${asset.sizeBytes / 1024} KB",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                FilledTonalButton(
                                    onClick = { onDownloadAsset(asset) },
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isApk) strings.githubInstallApk else strings.githubDownloadFile)
                                }
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = strings.githubCuratedTitle,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        curatedApps.forEach { app ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onFetch(app.repo) },
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(app.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            app.repo,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            if (isRu) app.descRu else app.descEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    FilledTonalButton(onClick = { onFetch(app.repo) }) {
                        Text(strings.githubFetchRelease)
                    }
                }
            }
        }
    }
}

@Composable
private fun RootFileManagerView(
    currentPath: String,
    entries: List<FileEntry>,
    accessMode: String,
    filePreview: Pair<String, String>?,
    strings: Strings,
    onLoadDir: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onPreviewFile: (FileEntry) -> Unit,
    onDismissPreview: () -> Unit,
    onCreateDir: (String) -> Unit,
    onDeleteEntry: (FileEntry) -> Unit,
    onOpenInTerminal: (String) -> Unit,
) {
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp),
    ) {
        // Quick bookmarks
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(
                "/" to "/",
                "/sdcard" to "/storage/emulated/0",
                "/data/local/tmp" to "/data/local/tmp",
                "/system" to "/system",
                "/etc" to "/system/etc",
            ).forEach { (label, target) ->
                FilterChip(
                    selected = currentPath == target,
                    onClick = { onLoadDir(target) },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily)) },
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Up")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentPath,
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${strings.filesRootBanner} $accessMode",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(onClick = { showNewFolderDialog = true }) {
                    Icon(Icons.Rounded.CreateNewFolder, contentDescription = "New Folder")
                }
                IconButton(onClick = { onOpenInTerminal(currentPath) }) {
                    Icon(Icons.Rounded.Terminal, contentDescription = "Open in Terminal")
                }
                IconButton(onClick = { onLoadDir(currentPath) }) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(entries, key = { it.fullPath }) { entry ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            if (entry.isDirectory) onLoadDir(entry.fullPath) else onPreviewFile(entry)
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (entry.isDirectory) Icons.Rounded.Folder else Icons.Rounded.Description,
                            contentDescription = null,
                            tint = if (entry.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.name,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "${entry.permissions} • ${entry.owner} • ${entry.sizeLabel}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { onDeleteEntry(entry) }) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("mkdir") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    singleLine = true,
                    placeholder = { Text("folder_name") },
                )
            },
            confirmButton = {
                Button(onClick = {
                    onCreateDir(newFolderName)
                    newFolderName = ""
                    showNewFolderDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) {
                    Text(strings.closeBtn)
                }
            },
        )
    }

    if (filePreview != null) {
        AlertDialog(
            onDismissRequest = onDismissPreview,
            title = { Text(filePreview.first, style = MaterialTheme.typography.titleMedium) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = filePreview.second,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissPreview) {
                    Text(strings.closeBtn)
                }
            },
        )
    }
}
