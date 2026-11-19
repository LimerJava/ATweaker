package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppLanguage
import com.example.ui.MainTab
import com.example.ui.TweakerViewModel
import com.example.ui.components.DeviceStatusHeaderCard
import com.example.ui.components.FotaDetailsDialog
import com.example.ui.components.ModelSelectorDialog
import com.example.ui.components.OvalFloatingBottomBar
import com.example.ui.components.ShizukuConnectionDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.strings.stringsFor
import com.example.ui.theme.ATweakerTheme

class MainActivity : ComponentActivity() {

    companion object {
        const val ACTION_OPEN_TERMINAL = "com.example.action.OPEN_TERMINAL"
    }

    private val viewModel: TweakerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleLaunchIntent(intent)

        setContent {
            ATweakerApp(viewModel = viewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleLaunchIntent(intent)
    }

    private fun handleLaunchIntent(intent: Intent?) {
        if (intent?.action == ACTION_OPEN_TERMINAL) {
            viewModel.selectTab(MainTab.TERMINAL)
        }
    }
}

@Composable
fun ATweakerApp(viewModel: TweakerViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val terminalSubTab by viewModel.terminalSubTab.collectAsStateWithLifecycle()
    val deviceInfo by viewModel.deviceInfo.collectAsStateWithLifecycle()
    val updateResult by viewModel.updateResult.collectAsStateWithLifecycle()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsStateWithLifecycle()
    val shizukuState by viewModel.shizukuState.collectAsStateWithLifecycle()
    val availableTweaks by viewModel.availableTweaks.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val tweakValues by viewModel.tweakValues.collectAsStateWithLifecycle()
    val lastLog by viewModel.lastLog.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    val terminalLines by viewModel.terminalLines.collectAsStateWithLifecycle()
    val installedPackages by viewModel.installedPackages.collectAsStateWithLifecycle()
    val commandHistory by viewModel.commandHistory.collectAsStateWithLifecycle()
    val terminalCurrentDir by viewModel.terminalCurrentDir.collectAsStateWithLifecycle()
    val githubRepoInput by viewModel.githubRepoInput.collectAsStateWithLifecycle()
    val githubLoading by viewModel.githubLoading.collectAsStateWithLifecycle()
    val githubRelease by viewModel.githubRelease.collectAsStateWithLifecycle()
    val githubMessage by viewModel.githubMessage.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val fileBrowserPath by viewModel.fileBrowserPath.collectAsStateWithLifecycle()
    val fileEntries by viewModel.fileEntries.collectAsStateWithLifecycle()
    val fileAccessMode by viewModel.fileAccessMode.collectAsStateWithLifecycle()
    val filePreview by viewModel.filePreviewContent.collectAsStateWithLifecycle()

    val isRu = settings.language == AppLanguage.RU
    val strings = remember(settings.language) { stringsFor(settings.language) }

    var showModelDialog by remember { mutableStateOf(false) }
    var showShizukuDialog by remember { mutableStateOf(false) }
    var showFotaDialog by remember { mutableStateOf(false) }

    if (selectedTab != MainTab.HOME) {
        BackHandler {
            viewModel.selectTab(MainTab.HOME)
        }
    }

    ATweakerTheme(
        themeMode = settings.themeMode,
        accentPreset = settings.accentPreset,
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    DeviceStatusHeaderCard(
                        deviceInfo = deviceInfo,
                        updateResult = updateResult,
                        isCheckingUpdate = isCheckingUpdate,
                        shizukuState = shizukuState,
                        strings = strings,
                        onShizukuClick = {
                            viewModel.shizukuManager.refreshStatus()
                            showShizukuDialog = true
                        },
                        onModelClick = { showModelDialog = true },
                        onUpdateBadgeClick = { showFotaDialog = true },
                    )

                    Crossfade(
                        targetState = selectedTab,
                        modifier = Modifier.weight(1f),
                        label = "tabCrossfade",
                    ) { tab ->
                        when (tab) {
                            MainTab.HOME -> HomeScreen(
                                deviceInfo = deviceInfo,
                                tweaks = availableTweaks,
                                selectedCategory = selectedCategory,
                                tweakValues = tweakValues,
                                lastLog = lastLog,
                                showDangerous = settings.showDangerousTweaks,
                                isRu = isRu,
                                strings = strings,
                                bottomPadding = innerPadding,
                                onSelectCategory = viewModel::selectCategory,
                                onRefreshStates = viewModel::refreshTweakStates,
                                onToggleTweak = { t, en -> viewModel.toggleTweak(t, en, isRu) },
                                onSelectOption = { t, opt -> viewModel.applyTweakOption(t, opt, isRu) },
                                onRunAction = { t -> viewModel.runTweakAction(t, isRu) },
                            )

                            MainTab.SEARCH -> SearchScreen(
                                query = searchQuery,
                                results = searchResults,
                                tweakValues = tweakValues,
                                isRu = isRu,
                                strings = strings,
                                bottomPadding = innerPadding,
                                onQueryChange = viewModel::updateSearchQuery,
                                onToggleTweak = { t, en -> viewModel.toggleTweak(t, en, isRu) },
                                onSelectOption = { t, opt -> viewModel.applyTweakOption(t, opt, isRu) },
                                onRunAction = { t -> viewModel.runTweakAction(t, isRu) },
                            )

                            MainTab.SETTINGS -> SettingsScreen(
                                settings = settings,
                                strings = strings,
                                bottomPadding = innerPadding,
                                onSelectTheme = viewModel::setThemeMode,
                                onSelectAccent = viewModel::setAccentPreset,
                                onSelectLanguage = viewModel::setLanguage,
                                onToggleDangerous = viewModel::setShowDangerous,
                                onPinTerminalShortcut = {
                                    viewModel.pinTerminalShortcut(
                                        shortLabel = strings.tabTerminal,
                                        longLabel = "A-Tweaker ${strings.tabTerminal}",
                                    )
                                },
                                onOpenUrl = viewModel::openExternalUrl,
                            )

                            MainTab.TERMINAL -> TerminalScreen(
                                subTab = terminalSubTab,
                                terminalLines = terminalLines,
                                installedPackages = installedPackages,
                                commandHistory = commandHistory,
                                currentDir = terminalCurrentDir,
                                githubRepoInput = githubRepoInput,
                                githubLoading = githubLoading,
                                githubRelease = githubRelease,
                                githubMessage = githubMessage,
                                downloadProgress = downloadProgress,
                                curatedApps = viewModel.getCuratedGitHubApps(),
                                fileBrowserPath = fileBrowserPath,
                                fileEntries = fileEntries,
                                fileAccessMode = fileAccessMode,
                                filePreview = filePreview,
                                isRu = isRu,
                                strings = strings,
                                bottomPadding = innerPadding,
                                onSelectSubTab = viewModel::selectTerminalSubTab,
                                onRunCommand = viewModel::runTerminalCommand,
                                onInstallPackage = viewModel::installTerminalPackage,
                                onRemovePackage = viewModel::removeTerminalPackage,
                                onGithubInputChange = viewModel::updateGithubInput,
                                onFetchGithubRelease = viewModel::fetchGitHubRelease,
                                onDownloadGithubAsset = viewModel::downloadAndInstallGitHubAsset,
                                onLoadFileDir = viewModel::loadFileDirectory,
                                onNavigateUpDir = viewModel::navigateUpDirectory,
                                onPreviewFile = viewModel::previewFile,
                                onDismissFilePreview = viewModel::dismissFilePreview,
                                onCreateDir = viewModel::createDirectoryInBrowser,
                                onDeleteEntry = viewModel::deleteEntryInBrowser,
                                onOpenPathInTerminal = viewModel::openPathInTerminal,
                            )
                        }
                    }
                }

                OvalFloatingBottomBar(
                    selectedTab = selectedTab,
                    strings = strings,
                    onSelectTab = viewModel::selectTab,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }

        if (showModelDialog) {
            ModelSelectorDialog(
                deviceInfo = deviceInfo,
                strings = strings,
                onSelectProfile = { profile -> viewModel.setOverrideProfile(profile?.id) },
                onDismiss = { showModelDialog = false },
            )
        }

        if (showShizukuDialog) {
            ShizukuConnectionDialog(
                shizukuState = shizukuState,
                strings = strings,
                onRequestPermission = { viewModel.shizukuManager.requestPermissionOrBind() },
                onOpenShizuku = { viewModel.shizukuManager.openShizukuApp() },
                onRefresh = { viewModel.shizukuManager.refreshStatus() },
                onDismiss = { showShizukuDialog = false },
            )
        }

        if (showFotaDialog) {
            FotaDetailsDialog(
                deviceInfo = deviceInfo,
                updateResult = updateResult,
                strings = strings,
                onRecheck = { viewModel.checkUpdatesFor() },
                onDismiss = { showFotaDialog = false },
            )
        }
    }
}
