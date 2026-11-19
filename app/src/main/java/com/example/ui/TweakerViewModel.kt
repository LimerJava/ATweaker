package com.example.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MainActivity
import com.example.R
import com.example.data.AccentColorPreset
import com.example.data.AppLanguage
import com.example.data.AppThemeMode
import com.example.data.DeviceInfo
import com.example.data.DeviceInspector
import com.example.data.FotaUpdateChecker
import com.example.data.SamsungTweaksCatalog
import com.example.data.SettingsRepository
import com.example.data.SynonymSearchEngine
import com.example.data.UpdateCheckResult
import com.example.data.UserSettings
import com.example.model.TweakCategory
import com.example.model.TweakItem
import com.example.model.TweakOption
import com.example.model.TweakExecutionLog
import com.example.shizuku.ShizukuManager
import com.example.shizuku.ShizukuState
import com.example.terminal.FileEntry
import com.example.terminal.GitHubInstaller
import com.example.terminal.GitHubReleaseAsset
import com.example.terminal.GitHubReleaseInfo
import com.example.terminal.InstalledPackage
import com.example.terminal.RootFileManager
import com.example.terminal.TerminalLine
import com.example.terminal.TerminalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    HOME,
    SEARCH,
    SETTINGS,
    TERMINAL,
}

enum class TerminalSubTab {
    SHELL,
    PACKAGES,
    GITHUB,
    FILES,
}

class TweakerViewModel(application: Application) : AndroidViewModel(application) {

    val shizukuManager = ShizukuManager(application)
    private val settingsRepo = SettingsRepository(application)
    private val fotaChecker = FotaUpdateChecker()
    private val gitHubInstaller = GitHubInstaller(application)
    val terminalRepo = TerminalRepository(application, shizukuManager, gitHubInstaller)
    private val rootFileManager = RootFileManager(shizukuManager)

    val settings: StateFlow<UserSettings> = settingsRepo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    val shizukuState: StateFlow<ShizukuState> = shizukuManager.state

    private val _selectedTab = MutableStateFlow(MainTab.HOME)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _terminalSubTab = MutableStateFlow(TerminalSubTab.SHELL)
    val terminalSubTab: StateFlow<TerminalSubTab> = _terminalSubTab.asStateFlow()

    private val _deviceInfo = MutableStateFlow(DeviceInspector.inspect())
    val deviceInfo: StateFlow<DeviceInfo> = _deviceInfo.asStateFlow()

    private val _updateResult = MutableStateFlow<UpdateCheckResult?>(null)
    val updateResult: StateFlow<UpdateCheckResult?> = _updateResult.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    private val _selectedCategory = MutableStateFlow<TweakCategory?>(null)
    val selectedCategory: StateFlow<TweakCategory?> = _selectedCategory.asStateFlow()

    private val _tweakValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val tweakValues: StateFlow<Map<String, String>> = _tweakValues.asStateFlow()

    private val _lastLog = MutableStateFlow<TweakExecutionLog?>(null)
    val lastLog: StateFlow<TweakExecutionLog?> = _lastLog.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Terminal shell state
    private val _terminalLines = MutableStateFlow<List<TerminalLine>>(
        listOf(
            TerminalLine(1L, "A-Tweaker Persistent Termux Shell v2.4", isAccent = true),
            TerminalLine(2L, "Packages & files persist across restarts. Type 'help' or 'fastfetch'."),
        )
    )
    val terminalLines: StateFlow<List<TerminalLine>> = _terminalLines.asStateFlow()

    val installedPackages: StateFlow<List<InstalledPackage>> = terminalRepo.installedPackages
    val terminalCurrentDir: StateFlow<String> = terminalRepo.currentDir
    val commandHistory: StateFlow<List<String>> = terminalRepo.commandHistory

    // GitHub Installer state
    private val _githubRepoInput = MutableStateFlow("")
    val githubRepoInput: StateFlow<String> = _githubRepoInput.asStateFlow()

    private val _githubLoading = MutableStateFlow(false)
    val githubLoading: StateFlow<Boolean> = _githubLoading.asStateFlow()

    private val _githubRelease = MutableStateFlow<GitHubReleaseInfo?>(null)
    val githubRelease: StateFlow<GitHubReleaseInfo?> = _githubRelease.asStateFlow()

    private val _githubMessage = MutableStateFlow<String?>(null)
    val githubMessage: StateFlow<String?> = _githubMessage.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

    // Root File Manager state
    private val _fileBrowserPath = MutableStateFlow(terminalRepo.homeDir.absolutePath)
    val fileBrowserPath: StateFlow<String> = _fileBrowserPath.asStateFlow()

    private val _fileEntries = MutableStateFlow<List<FileEntry>>(emptyList())
    val fileEntries: StateFlow<List<FileEntry>> = _fileEntries.asStateFlow()

    private val _fileAccessMode = MutableStateFlow("Local")
    val fileAccessMode: StateFlow<String> = _fileAccessMode.asStateFlow()

    private val _filePreviewContent = MutableStateFlow<Pair<String, String>?>(null)
    val filePreviewContent: StateFlow<Pair<String, String>?> = _filePreviewContent.asStateFlow()

    val availableTweaks: StateFlow<List<TweakItem>> = combine(
        _deviceInfo,
        settings,
    ) { info, userSettings ->
        SamsungTweaksCatalog.tweaksForDevice(
            profile = info.activeProfile,
            sdkInt = info.sdkInt,
            includeDangerous = userSettings.showDangerousTweaks,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val searchResults: StateFlow<List<Pair<TweakItem, List<String>>>> = combine(
        _searchQuery,
        availableTweaks,
    ) { query, tweaks ->
        SynonymSearchEngine.search(query, tweaks)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        shizukuManager.register()
        viewModelScope.launch {
            settings.collect { userSettings ->
                val inspected = DeviceInspector.inspect(userSettings.overrideProfileId)
                _deviceInfo.value = inspected
                checkUpdatesFor(inspected)
            }
        }
        viewModelScope.launch {
            shizukuState.collect { st ->
                if (st.isPrivilegedReady) {
                    refreshTweakStates()
                }
            }
        }
        loadFileDirectory(_fileBrowserPath.value)
    }

    override fun onCleared() {
        super.onCleared()
        shizukuManager.unregister()
    }

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    fun selectTerminalSubTab(subTab: TerminalSubTab) {
        _terminalSubTab.value = subTab
        if (subTab == TerminalSubTab.FILES) {
            loadFileDirectory(_fileBrowserPath.value)
        }
    }

    fun selectCategory(category: TweakCategory?) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun checkUpdatesFor(info: DeviceInfo = _deviceInfo.value) {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            _updateResult.value = fotaChecker.checkForUpdate(info)
            _isCheckingUpdate.value = false
        }
    }

    fun refreshTweakStates() {
        viewModelScope.launch(Dispatchers.IO) {
            val map = _tweakValues.value.toMutableMap()
            availableTweaks.value.forEach { tweak ->
                val readCmd = tweak.readCommand ?: return@forEach
                val res = shizukuManager.execPrivileged(readCmd)
                if (res.isSuccess) {
                    val cleaned = res.output.trim().lines().lastOrNull()?.trim().orEmpty()
                    if (cleaned.isNotEmpty() && cleaned != "null") {
                        map[tweak.id] = cleaned
                    } else if (cleaned == "null") {
                        map.remove(tweak.id)
                    }
                }
            }
            _tweakValues.value = map
        }
    }

    fun toggleTweak(tweak: TweakItem, enable: Boolean, isRu: Boolean) {
        val cmd = if (enable) tweak.enableCommand else tweak.disableCommand
        if (cmd.isNullOrBlank()) return
        executeTweakCommand(
            tweak = tweak,
            command = cmd,
            newValue = if (enable) (tweak.expectedEnabledValue ?: "1") else "0",
            isRu = isRu,
        )
    }

    fun applyTweakOption(tweak: TweakItem, option: TweakOption, isRu: Boolean) {
        executeTweakCommand(
            tweak = tweak,
            command = option.command,
            newValue = option.expectedReadValue ?: option.id,
            isRu = isRu,
        )
    }

    fun runTweakAction(tweak: TweakItem, isRu: Boolean) {
        val cmd = tweak.actionCommand ?: return
        executeTweakCommand(
            tweak = tweak,
            command = cmd,
            newValue = "executed",
            isRu = isRu,
        )
    }

    private fun executeTweakCommand(
        tweak: TweakItem,
        command: String,
        newValue: String,
        isRu: Boolean,
    ) {
        viewModelScope.launch {
            val res = shizukuManager.execPrivileged(command)
            if (res.isSuccess) {
                val updated = _tweakValues.value.toMutableMap()
                updated[tweak.id] = newValue
                _tweakValues.value = updated
            }
            _lastLog.value = TweakExecutionLog(
                tweakId = tweak.id,
                title = if (isRu) tweak.titleRu else tweak.titleEn,
                command = command,
                output = res.output.ifBlank {
                    if (res.isSuccess) "OK (${res.privilegeMode})" else "Exit ${res.exitCode}"
                },
                success = res.isSuccess,
                privilegeMode = res.privilegeMode,
            )
        }
    }

    // Settings actions
    fun setThemeMode(mode: AppThemeMode) = viewModelScope.launch { settingsRepo.setThemeMode(mode) }
    fun setAccentPreset(preset: AccentColorPreset) = viewModelScope.launch { settingsRepo.setAccentPreset(preset) }
    fun setLanguage(lang: AppLanguage) = viewModelScope.launch { settingsRepo.setLanguage(lang) }
    fun setShowDangerous(show: Boolean) = viewModelScope.launch { settingsRepo.setShowDangerousTweaks(show) }
    fun setOverrideProfile(profileId: String?) = viewModelScope.launch { settingsRepo.setOverrideProfileId(profileId) }

    fun pinTerminalShortcut(shortLabel: String, longLabel: String): Boolean {
        val ctx = getApplication<Application>()
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(ctx)) return false
        val intent = Intent(ctx, MainActivity::class.java).apply {
            action = MainActivity.ACTION_OPEN_TERMINAL
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val shortcut = ShortcutInfoCompat.Builder(ctx, "atweaker_terminal_shortcut")
            .setShortLabel(shortLabel)
            .setLongLabel(longLabel)
            .setIcon(IconCompat.createWithResource(ctx, R.mipmap.ic_launcher))
            .setIntent(intent)
            .build()
        return ShortcutManagerCompat.requestPinShortcut(ctx, shortcut, null)
    }

    // Terminal Shell actions
    fun runTerminalCommand(command: String) {
        val trimmed = command.trim()
        if (trimmed.isEmpty()) return
        if (trimmed.equals("clear", ignoreCase = true)) {
            _terminalLines.value = emptyList()
            return
        }
        val prompt = "${_deviceInfo.value.activeProfile.id.lowercase()}:${
            _currentDirShort()
        }$ $trimmed"
        _terminalLines.value = _terminalLines.value + TerminalLine(
            id = System.nanoTime(),
            text = prompt,
            isCommand = true,
        )
        viewModelScope.launch {
            val out = terminalRepo.executeCommand(trimmed, _deviceInfo.value)
            _terminalLines.value = (_terminalLines.value + out).takeLast(350)
        }
    }

    private fun _currentDirShort(): String {
        val cur = terminalCurrentDir.value
        val home = terminalRepo.homeDir.absolutePath
        return if (cur == home) "~" else cur.substringAfterLast('/').ifBlank { "/" }
    }

    fun installTerminalPackage(pkgId: String) {
        viewModelScope.launch {
            val res = terminalRepo.installPackage(pkgId)
            res.onSuccess { msg ->
                _terminalLines.value = _terminalLines.value + TerminalLine(System.nanoTime(), msg, isAccent = true)
            }.onFailure { err ->
                _terminalLines.value = _terminalLines.value + TerminalLine(System.nanoTime(), err.message ?: "Error", isError = true)
            }
        }
    }

    fun removeTerminalPackage(pkgId: String) {
        viewModelScope.launch {
            val res = terminalRepo.removePackage(pkgId)
            res.onSuccess { msg ->
                _terminalLines.value = _terminalLines.value + TerminalLine(System.nanoTime(), msg)
            }
        }
    }

    // GitHub Installer actions
    fun updateGithubInput(value: String) {
        _githubRepoInput.value = value
    }

    fun fetchGitHubRelease(repoInput: String = _githubRepoInput.value) {
        _githubRepoInput.value = repoInput
        viewModelScope.launch(Dispatchers.IO) {
            _githubLoading.value = true
            _githubMessage.value = null
            val res = gitHubInstaller.fetchLatestRelease(repoInput)
            res.onSuccess {
                _githubRelease.value = it
            }.onFailure {
                _githubRelease.value = null
                _githubMessage.value = it.message ?: "Failed to fetch release"
            }
            _githubLoading.value = false
        }
    }

    fun downloadAndInstallGitHubAsset(asset: GitHubReleaseAsset) {
        viewModelScope.launch(Dispatchers.IO) {
            _downloadProgress.value = 0f
            _githubMessage.value = "Downloading ${asset.name}..."
            val dl = gitHubInstaller.downloadAsset(asset, terminalRepo.downloadsDir) { p ->
                _downloadProgress.value = p
            }
            _downloadProgress.value = null
            dl.onSuccess { file ->
                if (file.name.endsWith(".apk", ignoreCase = true)) {
                    _githubMessage.value = terminalRepo.installApkFile(file)
                } else {
                    _githubMessage.value = "Saved to ${file.absolutePath}"
                }
            }.onFailure { err ->
                _githubMessage.value = err.message ?: "Download failed"
            }
        }
    }

    fun getCuratedGitHubApps() = gitHubInstaller.curatedApps

    // Root File Manager actions
    fun loadFileDirectory(path: String) {
        viewModelScope.launch {
            _fileBrowserPath.value = path
            val (entries, mode) = rootFileManager.listDirectory(path)
            _fileEntries.value = entries
            _fileAccessMode.value = mode
        }
    }

    fun navigateUpDirectory() {
        val current = _fileBrowserPath.value.trimEnd('/')
        if (current.isEmpty() || current == "/") return
        val parent = current.substringBeforeLast('/').ifEmpty { "/" }
        loadFileDirectory(parent)
    }

    fun previewFile(entry: FileEntry) {
        viewModelScope.launch {
            val content = rootFileManager.readFilePreview(entry.fullPath)
            _filePreviewContent.value = entry.name to content
        }
    }

    fun dismissFilePreview() {
        _filePreviewContent.value = null
    }

    fun createDirectoryInBrowser(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            rootFileManager.createDirectory(_fileBrowserPath.value, name)
            loadFileDirectory(_fileBrowserPath.value)
        }
    }

    fun deleteEntryInBrowser(entry: FileEntry) {
        viewModelScope.launch {
            rootFileManager.deletePath(entry.fullPath)
            loadFileDirectory(_fileBrowserPath.value)
        }
    }

    fun openPathInTerminal(path: String) {
        terminalRepo.setWorkingDirectory(path)
        _terminalSubTab.value = TerminalSubTab.SHELL
        runTerminalCommand("cd '$path' && ls -la")
    }

    fun openExternalUrl(url: String) {
        val ctx = getApplication<Application>()
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(intent)
        } catch (_: Throwable) {
        }
    }
}
