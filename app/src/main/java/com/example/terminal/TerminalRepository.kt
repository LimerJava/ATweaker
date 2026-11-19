package com.example.terminal

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.SystemClock
import androidx.core.content.FileProvider
import com.example.data.DeviceInfo
import com.example.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class InstalledPackage(
    val id: String,
    val name: String,
    val version: String,
    val installedAt: String,
)

data class TerminalLine(
    val id: Long,
    val text: String,
    val isCommand: Boolean = false,
    val isError: Boolean = false,
    val isAccent: Boolean = false,
)

class TerminalRepository(
    private val context: Context,
    private val shizukuManager: ShizukuManager,
    private val gitHubInstaller: GitHubInstaller,
) {
    private val rootDir = File(context.filesDir, "termux_env")
    val homeDir = File(rootDir, "home")
    private val usrBinDir = File(rootDir, "usr/bin")
    private val etcDir = File(rootDir, "etc")
    val downloadsDir = File(rootDir, "downloads")
    private val pkgRegistryFile = File(etcDir, "packages.json")
    private val historyFile = File(etcDir, "history.txt")
    private val bashrcFile = File(homeDir, ".bashrc")

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val _installedPackages = MutableStateFlow<List<InstalledPackage>>(emptyList())
    val installedPackages: StateFlow<List<InstalledPackage>> = _installedPackages.asStateFlow()

    private val _currentDir = MutableStateFlow("")
    val currentDir: StateFlow<String> = _currentDir.asStateFlow()

    private val _commandHistory = MutableStateFlow<List<String>>(emptyList())
    val commandHistory: StateFlow<List<String>> = _commandHistory.asStateFlow()

    private val envVars = mutableMapOf<String, String>()

    init {
        ensureEnvironmentInitialized()
        loadInstalledPackages()
        loadHistory()
    }

    private fun ensureEnvironmentInitialized() {
        listOf(rootDir, homeDir, usrBinDir, etcDir, downloadsDir).forEach {
            if (!it.exists()) it.mkdirs()
        }
        if (!bashrcFile.exists()) {
            bashrcFile.writeText(
                """
                # A-Tweaker Persistent Termux Environment
                export PREFIX="${rootDir.absolutePath}/usr"
                export HOME="${homeDir.absolutePath}"
                export TERM="xterm-256color"
                """.trimIndent() + "\n"
            )
        }
        if (!pkgRegistryFile.exists()) {
            // Pre-install core base utilities only once; user-installed packages persist forever
            val initial = JSONArray().apply {
                put(packageToJson("busybox-utils", "busybox-utils", "1.36.1"))
            }
            pkgRegistryFile.writeText(initial.toString(2))
        }
        _currentDir.value = homeDir.absolutePath
        envVars["HOME"] = homeDir.absolutePath
        envVars["PREFIX"] = File(rootDir, "usr").absolutePath
        envVars["PATH"] = "${usrBinDir.absolutePath}:/system/bin:/system/xbin"
        envVars["TERM"] = "xterm-256color"
    }

    private fun packageToJson(id: String, name: String, version: String): JSONObject {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("version", version)
            put("installedAt", dateStr)
        }
    }

    private fun loadInstalledPackages() {
        val list = mutableListOf<InstalledPackage>()
        try {
            val arr = JSONArray(pkgRegistryFile.readText())
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    InstalledPackage(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        version = obj.getString("version"),
                        installedAt = obj.optString("installedAt", ""),
                    )
                )
            }
        } catch (_: Throwable) {
        }
        _installedPackages.value = list
    }

    private fun saveInstalledPackages(list: List<InstalledPackage>) {
        val arr = JSONArray()
        list.forEach { pkg ->
            arr.put(
                JSONObject().apply {
                    put("id", pkg.id)
                    put("name", pkg.name)
                    put("version", pkg.version)
                    put("installedAt", pkg.installedAt)
                }
            )
        }
        pkgRegistryFile.writeText(arr.toString(2))
        _installedPackages.value = list
    }

    private fun loadHistory() {
        if (!historyFile.exists()) return
        _commandHistory.value = historyFile.readLines().filter { it.isNotBlank() }.takeLast(100)
    }

    private fun appendHistory(cmd: String) {
        val current = _commandHistory.value.toMutableList()
        if (current.lastOrNull() != cmd) {
            current.add(cmd)
            val trimmed = current.takeLast(100)
            _commandHistory.value = trimmed
            historyFile.writeText(trimmed.joinToString("\n") + "\n")
        }
    }

    fun isPackageInstalled(pkgIdOrCmd: String): Boolean {
        val pkg = TerminalPackageCatalog.findPackage(pkgIdOrCmd) ?: return false
        return _installedPackages.value.any { it.id == pkg.id }
    }

    suspend fun installPackage(pkgId: String): Result<String> = withContext(Dispatchers.IO) {
        val pkg = TerminalPackageCatalog.findPackage(pkgId)
            ?: return@withContext Result.failure(IllegalArgumentException("Package '$pkgId' not found in repository."))

        if (_installedPackages.value.any { it.id == pkg.id }) {
            return@withContext Result.success("${pkg.name} (${pkg.version}) is already installed.")
        }

        // Write persistent executable stub(s) in usr/bin so they remain on disk across restarts
        pkg.binaryCommands.forEach { binName ->
            val binFile = File(usrBinDir, binName)
            binFile.writeText("#!/system/bin/sh\n# A-Tweaker Persistent Package: ${pkg.name} ${pkg.version}\necho \"${pkg.name} ${pkg.version}\"\n")
            binFile.setExecutable(true)
        }

        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        val updated = _installedPackages.value + InstalledPackage(
            id = pkg.id,
            name = pkg.name,
            version = pkg.version,
            installedAt = dateStr,
        )
        saveInstalledPackages(updated)

        Result.success("Unpacking ${pkg.name} (${pkg.version}) -> ${usrBinDir.absolutePath}\nSetting up ${pkg.name} ... Done (persisted).")
    }

    suspend fun removePackage(pkgId: String): Result<String> = withContext(Dispatchers.IO) {
        val pkg = TerminalPackageCatalog.findPackage(pkgId)
            ?: return@withContext Result.failure(IllegalArgumentException("Unknown package: $pkgId"))
        pkg.binaryCommands.forEach { File(usrBinDir, it).delete() }
        val updated = _installedPackages.value.filterNot { it.id == pkg.id }
        saveInstalledPackages(updated)
        Result.success("Removed ${pkg.name} and cleaned ${usrBinDir.absolutePath}")
    }

    suspend fun installApkFile(apkFile: File): String = withContext(Dispatchers.IO) {
        if (!apkFile.exists()) return@withContext "File not found: ${apkFile.absolutePath}"
        val shizukuStatus = shizukuManager.state.value
        if (shizukuStatus.isPrivilegedReady) {
            val tmpPath = "/data/local/tmp/${apkFile.name}"
            // Copy from app sandbox or install via cat stream
            val catCmd = "cat '${apkFile.absolutePath}' > '$tmpPath' && pm install -r '$tmpPath'; rm -f '$tmpPath'"
            val res = shizukuManager.execPrivileged(catCmd)
            if (res.output.contains("Success", ignoreCase = true) || res.isSuccess) {
                return@withContext "Installed via ${res.privilegeMode}: ${res.output.ifBlank { "Success" }}"
            }
        }

        // Launch standard Android package installer intent via FileProvider
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile,
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            "Opened Android Package Installer for ${apkFile.name}"
        } catch (t: Throwable) {
            "Saved to ${apkFile.absolutePath}. Tap to install (${t.message})"
        }
    }

    fun setWorkingDirectory(path: String) {
        _currentDir.value = path
    }

    suspend fun executeCommand(
        rawCommand: String,
        deviceInfo: DeviceInfo,
    ): List<TerminalLine> = withContext(Dispatchers.IO) {
        val cmd = rawCommand.trim()
        if (cmd.isEmpty()) return@withContext emptyList()
        appendHistory(cmd)

        val parts = tokenize(cmd)
        val head = parts.firstOrNull()?.lowercase().orEmpty()
        val args = parts.drop(1)

        val outLines = mutableListOf<TerminalLine>()
        var nextId = System.nanoTime()
        fun addOut(text: String, isError: Boolean = false, isAccent: Boolean = false) {
            text.lines().forEach { line ->
                outLines.add(TerminalLine(nextId++, line, isError = isError, isAccent = isAccent))
            }
        }

        when (head) {
            "help" -> {
                addOut("A-Tweaker Persistent Termux Shell (packages saved across restarts)", isAccent = true)
                addOut("Package Manager: pkg update | pkg list | pkg list-installed | pkg search <q> | pkg install <name> | pkg remove <name>")
                addOut("GitHub & Net:    gh-install <owner/repo> | git clone <repo> | curl <url> | wget <url>")
                addOut("System Tools:    fastfetch | neofetch | htop | saminfo | debloat-audit | net-probe | tree | jq | python")
                addOut("Filesystem/ADB:  cd <dir> | pwd | ls | cat | mkdir | rm | getprop | settings | pm | dumpsys | clear")
            }

            "pwd" -> addOut(_currentDir.value)

            "cd" -> {
                val target = args.firstOrNull() ?: homeDir.absolutePath
                val resolved = when {
                    target == "~" -> homeDir.absolutePath
                    target.startsWith("~/") -> "${homeDir.absolutePath}/${target.removePrefix("~/")}"
                    target == ".." -> File(_currentDir.value).parent ?: "/"
                    target.startsWith("/") -> target
                    else -> "${_currentDir.value.trimEnd('/')}/$target"
                }
                val file = File(resolved)
                if (file.exists() && file.isDirectory) {
                    _currentDir.value = file.canonicalPath
                } else {
                    val check = shizukuManager.execPrivileged("[ -d '$resolved' ] && echo OK")
                    if (check.output.contains("OK")) {
                        _currentDir.value = resolved
                    } else {
                        addOut("cd: $resolved: No such directory", isError = true)
                    }
                }
            }

            "export" -> {
                val assignment = args.joinToString(" ")
                val idx = assignment.indexOf('=')
                if (idx > 0) {
                    val k = assignment.substring(0, idx).trim()
                    val v = assignment.substring(idx + 1).trim().trim('"')
                    envVars[k] = v
                    addOut("export $k=$v")
                } else {
                    envVars.forEach { (k, v) -> addOut("$k=$v") }
                }
            }

            "env" -> envVars.forEach { (k, v) -> addOut("$k=$v") }

            "history" -> _commandHistory.value.forEachIndexed { idx, s ->
                addOut("${idx + 1}  $s")
            }

            "pkg", "apt" -> handlePkgCommand(args, ::addOut)

            "fastfetch", "neofetch" -> {
                if (!isPackageInstalled(head)) {
                    addOut("Command '$head' is not installed. Run: pkg install $head", isError = true)
                } else {
                    addOut(renderFastfetch(deviceInfo), isAccent = true)
                }
            }

            "htop" -> {
                if (!isPackageInstalled("htop")) {
                    addOut("Command 'htop' is not installed. Run: pkg install htop", isError = true)
                } else {
                    addOut(renderHtop(), isAccent = false)
                }
            }

            "saminfo", "csc-check" -> {
                if (!isPackageInstalled("samsung-csc-tools")) {
                    addOut("Install first: pkg install samsung-csc-tools", isError = true)
                } else {
                    addOut(renderSamsungInfo(deviceInfo), isAccent = true)
                }
            }

            "tree" -> {
                if (!isPackageInstalled("tree")) {
                    addOut("Install first: pkg install tree", isError = true)
                } else {
                    val target = args.firstOrNull() ?: _currentDir.value
                    addOut(renderTree(File(target), "", 0))
                }
            }

            "curl" -> {
                if (!isPackageInstalled("curl")) {
                    addOut("Install first: pkg install curl", isError = true)
                } else {
                    val url = args.firstOrNull { it.startsWith("http") }
                    if (url == null) {
                        addOut("Usage: curl <https://url>", isError = true)
                    } else {
                        try {
                            val req = Request.Builder().url(url).header("User-Agent", "A-Tweaker-curl/8.11").build()
                            httpClient.newCall(req).execute().use { resp ->
                                addOut("HTTP ${resp.code} ${resp.message}", isAccent = true)
                                addOut(resp.body?.string()?.take(4000).orEmpty())
                            }
                        } catch (t: Throwable) {
                            addOut("curl: ${t.message}", isError = true)
                        }
                    }
                }
            }

            "wget" -> {
                if (!isPackageInstalled("wget")) {
                    addOut("Install first: pkg install wget", isError = true)
                } else {
                    val url = args.firstOrNull { it.startsWith("http") }
                    if (url == null) {
                        addOut("Usage: wget <https://url>", isError = true)
                    } else {
                        val fileName = url.substringAfterLast('/').ifBlank { "download.bin" }
                        val asset = GitHubReleaseAsset(fileName, 0L, url, "application/octet-stream")
                        val res = gitHubInstaller.downloadAsset(asset, downloadsDir) {}
                        res.onSuccess { f ->
                            addOut("Saved to ${f.absolutePath} (${f.length()} bytes)", isAccent = true)
                        }.onFailure { err ->
                            addOut("wget: ${err.message}", isError = true)
                        }
                    }
                }
            }

            "gh-install" -> {
                if (!isPackageInstalled("gh-install")) {
                    addOut("Install first: pkg install gh-install", isError = true)
                } else {
                    val repo = args.firstOrNull()
                    if (repo == null) {
                        addOut("Usage: gh-install <owner/repo>", isError = true)
                    } else {
                        val rel = gitHubInstaller.fetchLatestRelease(repo)
                        rel.onSuccess { info ->
                            addOut("Found ${info.repoFullName} ${info.tagName}", isAccent = true)
                            val apk = info.assets.firstOrNull { it.name.endsWith(".apk", true) }
                            if (apk == null) {
                                addOut("No .apk asset in latest release. Available: ${info.assets.joinToString { it.name }}", isError = true)
                            } else {
                                addOut("Downloading ${apk.name}...")
                                val dl = gitHubInstaller.downloadAsset(apk, downloadsDir) {}
                                dl.onSuccess { file ->
                                    addOut("Downloaded ${file.name}. Installing...")
                                    addOut(installApkFile(file), isAccent = true)
                                }.onFailure { addOut("Download error: ${it.message}", isError = true) }
                            }
                        }.onFailure { addOut("gh-install: ${it.message}", isError = true) }
                    }
                }
            }

            "git" -> {
                if (!isPackageInstalled("git")) {
                    addOut("Install first: pkg install git", isError = true)
                } else if (args.firstOrNull() == "clone" && args.size >= 2) {
                    val repo = args[1]
                    val rel = gitHubInstaller.fetchLatestRelease(repo)
                    rel.onSuccess { info ->
                        val destDir = File(homeDir, info.repoFullName.substringAfter('/'))
                        destDir.mkdirs()
                        File(destDir, "RELEASE.md").writeText("# ${info.repoFullName} (${info.tagName})\n\n${info.body}\n")
                        addOut("Cloned metadata & latest release (${info.tagName}) into ${destDir.absolutePath}", isAccent = true)
                    }.onFailure {
                        addOut("git clone: ${it.message}", isError = true)
                    }
                } else {
                    addOut("git version 2.47.1 (Usage: git clone <owner/repo>)")
                }
            }

            "jq" -> {
                if (!isPackageInstalled("jq")) {
                    addOut("Install first: pkg install jq", isError = true)
                } else {
                    val path = args.lastOrNull()
                    val file = path?.let { File(if (it.startsWith("/")) it else "${_currentDir.value}/$it") }
                    if (file != null && file.exists()) {
                        try {
                            val formatted = JSONObject(file.readText()).toString(2)
                            addOut(formatted)
                        } catch (_: Throwable) {
                            addOut(file.readText())
                        }
                    } else {
                        addOut("Usage: jq <file.json>", isError = true)
                    }
                }
            }

            "python", "python3" -> {
                if (!isPackageInstalled("python-mini")) {
                    addOut("Install first: pkg install python-mini", isError = true)
                } else {
                    addOut("Python 3.12-lite on Android (${deviceInfo.arch})")
                    if (args.isNotEmpty()) {
                        addOut("Executed: ${args.joinToString(" ")}")
                    }
                }
            }

            "net-probe", "dns-check" -> {
                if (!isPackageInstalled("net-probe")) {
                    addOut("Install first: pkg install net-probe", isError = true)
                } else {
                    val res = shizukuManager.execReadOnly("ip addr show wlan0 2>/dev/null || ifconfig 2>/dev/null; getprop net.dns1; getprop net.dns2")
                    addOut(res.output.ifBlank { "No interface details available" })
                }
            }

            "debloat-audit" -> {
                if (!isPackageInstalled("a-series-debloater")) {
                    addOut("Install first: pkg install a-series-debloater", isError = true)
                } else {
                    val candidates = listOf(
                        "com.samsung.android.game.gos",
                        "com.samsung.android.bixby.agent",
                        "com.samsung.android.app.spage",
                        "com.facebook.services",
                        "com.facebook.appmanager",
                        "com.samsung.android.knox.analytics.uploader",
                    )
                    val res = shizukuManager.execPrivileged("pm list packages -e")
                    val enabled = res.output
                    addOut("=== Galaxy A Debloat Audit (${res.privilegeMode}) ===", isAccent = true)
                    candidates.forEach { pkg ->
                        val active = enabled.contains(pkg)
                        addOut("${if (active) "[ACTIVE]" else "[DISABLED/ABSENT]"} $pkg")
                    }
                }
            }

            else -> {
                val envPrefix = "export HOME='${homeDir.absolutePath}' PREFIX='${rootDir.absolutePath}/usr' PATH='${envVars["PATH"]}' && cd '${_currentDir.value}' 2>/dev/null; "
                val res = shizukuManager.execPrivileged(envPrefix + cmd)
                if (res.output.isNotBlank()) {
                    addOut(res.output, isError = !res.isSuccess)
                } else if (!res.isSuccess) {
                    addOut("Command exited with code ${res.exitCode} (${res.privilegeMode})", isError = true)
                }
            }
        }

        outLines
    }

    private suspend fun handlePkgCommand(
        args: List<String>,
        addOut: (String, Boolean, Boolean) -> Unit,
    ) {
        val sub = args.firstOrNull()?.lowercase().orEmpty()
        when (sub) {
            "update", "upgrade" -> {
                addOut("Hit:1 https://packages.atweaker.local/termux-aarch64 stable InRelease", false, true)
                addOut("Reading package lists... Done (${TerminalPackageCatalog.availablePackages.size} packages available, ${_installedPackages.value.size} installed persistently)", false, false)
            }

            "list", "search" -> {
                val q = args.drop(1).joinToString(" ").lowercase()
                TerminalPackageCatalog.availablePackages
                    .filter { q.isBlank() || it.name.contains(q, true) || it.descriptionEn.contains(q, true) }
                    .forEach { pkg ->
                        val mark = if (isPackageInstalled(pkg.id)) "[installed]" else "[available]"
                        addOut("${pkg.name}/${pkg.version} $mark - ${pkg.descriptionEn}", false, mark == "[installed]")
                    }
            }

            "list-installed" -> {
                if (_installedPackages.value.isEmpty()) {
                    addOut("No packages installed yet.", false, false)
                } else {
                    _installedPackages.value.forEach { pkg ->
                        addOut("${pkg.name} ${pkg.version} (installed ${pkg.installedAt})", false, true)
                    }
                }
            }

            "install", "add" -> {
                val targets = args.drop(1)
                if (targets.isEmpty()) {
                    addOut("Usage: pkg install <package-name>", true, false)
                } else {
                    targets.forEach { target ->
                        installPackage(target)
                            .onSuccess { addOut(it, false, true) }
                            .onFailure { addOut(it.message ?: "Failed", true, false) }
                    }
                }
            }

            "remove", "uninstall" -> {
                val targets = args.drop(1)
                if (targets.isEmpty()) {
                    addOut("Usage: pkg remove <package-name>", true, false)
                } else {
                    targets.forEach { target ->
                        removePackage(target)
                            .onSuccess { addOut(it, false, true) }
                            .onFailure { addOut(it.message ?: "Failed", true, false) }
                    }
                }
            }

            else -> {
                addOut("Usage: pkg [update | list | list-installed | search <q> | install <name> | remove <name>]", false, false)
            }
        }
    }

    private fun renderFastfetch(deviceInfo: DeviceInfo): String {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(memInfo)
        val totalMb = memInfo.totalMem / (1024 * 1024)
        val usedMb = (memInfo.totalMem - memInfo.availMem) / (1024 * 1024)

        val battIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = battIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val uptimeMin = SystemClock.elapsedRealtime() / 60000L
        val uptimeStr = "${uptimeMin / 60}h ${uptimeMin % 60}m"
        val profile = deviceInfo.activeProfile
        val shizukuStatus = shizukuManager.state.value

        return """
            |   ███████╗ █████╗ ███╗   ███╗   ${deviceInfo.manufacturer} ${profile.marketingName}
            |   ██╔════╝██╔══██╗████╗ ████║   ----------------------------------------
            |   ███████╗███████║██╔████╔██║   OS: Android ${deviceInfo.androidVersion} (API ${deviceInfo.sdkInt})
            |   ╚════██║██╔══██║██║╚██╔╝██║   UI: ${deviceInfo.oneUiVersion ?: profile.latestOneUiVersion}
            |   ███████║██║  ██║██║ ╚═╝ ██║   Model: ${deviceInfo.rawModel} [${profile.id}]
            |   ╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝   SoC: ${profile.socName} (${deviceInfo.arch})
            |                                 Kernel: ${deviceInfo.kernelVersion}
            |   Display: ${profile.maxRefreshRateHz}Hz ${profile.displayType.name}
            |   Memory:  ${usedMb} MiB / ${totalMb} MiB
            |   Battery: ${if (level >= 0) "$level%" else "N/A"} | Uptime: $uptimeStr
            |   Packages: ${_installedPackages.value.size} (persistent in pkg_registry)
            |   Shell:   A-Tweaker Termux | ADB: ${shizukuStatus.status.name}
        """.trimMargin()
    }

    private suspend fun renderHtop(): String {
        val res = shizukuManager.execReadOnly("ps -A -o PID,USER,RSS,NAME 2>/dev/null | head -n 18")
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(memInfo)
        val totalMb = memInfo.totalMem / (1024 * 1024)
        val availMb = memInfo.availMem / (1024 * 1024)
        return "Mem: ${totalMb - availMb}M / ${totalMb}M | Cores: ${Runtime.getRuntime().availableProcessors()}\n" +
            res.output.ifBlank { "Run with Shizuku connected to view full system process table." }
    }

    private fun renderSamsungInfo(deviceInfo: DeviceInfo): String {
        val p = deviceInfo.activeProfile
        return """
            |Samsung Galaxy A Diagnostics
            |  Active Profile : ${p.marketingName} (${p.id})
            |  Hardware Model : ${deviceInfo.rawModel} (${deviceInfo.deviceCodename})
            |  CSC Region     : ${deviceInfo.cscCode}
            |  One UI         : ${deviceInfo.oneUiVersion ?: "N/A"} (Target: ${p.latestOneUiVersion})
            |  SoC / Arch     : ${p.socName} / ${deviceInfo.arch}
            |  Panel          : ${p.displayType.name} @ ${p.maxRefreshRateHz}Hz
            |  5G Support     : ${p.supports5G} | One UI Core: ${p.isOneUiCore}
            |  Enhanced CPU   : ${p.supportsEnhancedProcessing} | RAM Plus: ${p.supportsRamPlus}
        """.trimMargin()
    }

    private fun renderTree(dir: File, indent: String, depth: Int): String {
        if (depth > 2) return ""
        val files = dir.listFiles()?.sortedBy { it.name } ?: return "$indent(unreadable)\n"
        val sb = StringBuilder()
        if (depth == 0) sb.append("${dir.absolutePath}\n")
        files.forEachIndexed { idx, f ->
            val last = idx == files.lastIndex
            val branch = if (last) "└── " else "├── "
            sb.append(indent).append(branch).append(f.name)
            if (f.isDirectory) sb.append('/')
            sb.append('\n')
            if (f.isDirectory && depth < 2) {
                val childIndent = indent + if (last) "    " else "│   "
                sb.append(renderTree(f, childIndent, depth + 1))
            }
        }
        return sb.toString().trimEnd()
    }

    private fun tokenize(input: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        for (ch in input) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch.isWhitespace() && !inQuotes -> {
                    if (current.isNotEmpty()) {
                        result.add(current.toString())
                        current.clear()
                    }
                }
                else -> current.append(ch)
            }
        }
        if (current.isNotEmpty()) result.add(current.toString())
        return result
    }
}
