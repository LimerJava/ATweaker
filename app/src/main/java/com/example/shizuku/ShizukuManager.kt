package com.example.shizuku

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import com.example.BuildConfig
import com.example.IShellService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

enum class ShizukuStatus {
    NOT_INSTALLED,
    DISCONNECTED,
    PERMISSION_REQUIRED,
    CONNECTING,
    CONNECTED_ADB,
    CONNECTED_ROOT,
}

data class ShizukuState(
    val status: ShizukuStatus = ShizukuStatus.DISCONNECTED,
    val shizukuVersion: Int? = null,
    val uid: Int? = null,
    val hasLocalRoot: Boolean = false,
) {
    val isPrivilegedReady: Boolean
        get() = status == ShizukuStatus.CONNECTED_ADB ||
            status == ShizukuStatus.CONNECTED_ROOT ||
            hasLocalRoot
}

data class ShellResult(
    val exitCode: Int,
    val output: String,
    val privilegeMode: String,
) {
    val isSuccess: Boolean get() = exitCode == 0
}

class ShizukuManager(private val context: Context) {

    companion object {
        private const val REQUEST_CODE_SHIZUKU = 1001
        private const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    }

    private val _state = MutableStateFlow(ShizukuState())
    val state: StateFlow<ShizukuState> = _state.asStateFlow()

    @Volatile
    private var shellService: IShellService? = null
    private var isBound = false

    private val userServiceArgs by lazy {
        Shizuku.UserServiceArgs(
            ComponentName(context.packageName, ShellUserService::class.java.name)
        )
            .daemon(false)
            .processNameSuffix("tweaker_shell")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val stub = IShellService.Stub.asInterface(service)
            shellService = stub
            isBound = true
            val remoteUid = try {
                stub?.uid
            } catch (_: Throwable) {
                null
            }
            val newStatus = when (remoteUid) {
                0 -> ShizukuStatus.CONNECTED_ROOT
                else -> ShizukuStatus.CONNECTED_ADB
            }
            _state.value = _state.value.copy(
                status = newStatus,
                uid = remoteUid,
                shizukuVersion = safeGetVersion(),
            )
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            shellService = null
            isBound = false
            refreshStatus()
        }
    }

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        refreshStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        shellService = null
        isBound = false
        refreshStatus()
    }

    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == REQUEST_CODE_SHIZUKU) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    bindShellService()
                } else {
                    refreshStatus()
                }
            }
        }

    fun register() {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
        } catch (_: Throwable) {
        }
        checkLocalSu()
        refreshStatus()
    }

    fun unregister() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
            if (isBound) {
                Shizuku.unbindUserService(userServiceArgs, serviceConnection, true)
                isBound = false
            }
        } catch (_: Throwable) {
        }
    }

    fun isShizukuAppInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun openShizukuApp(): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
                ?: return false
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun refreshStatus() {
        val binderAlive = try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }

        if (!binderAlive) {
            val status = if (isShizukuAppInstalled()) {
                ShizukuStatus.DISCONNECTED
            } else {
                ShizukuStatus.NOT_INSTALLED
            }
            _state.value = _state.value.copy(
                status = status,
                shizukuVersion = null,
                uid = null,
            )
            return
        }

        val permissionGranted = try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }

        if (!permissionGranted) {
            _state.value = _state.value.copy(
                status = ShizukuStatus.PERMISSION_REQUIRED,
                shizukuVersion = safeGetVersion(),
                uid = null,
            )
            return
        }

        if (shellService != null && isBound) {
            val uid = try {
                shellService?.uid
            } catch (_: Throwable) {
                null
            }
            if (uid != null) {
                _state.value = _state.value.copy(
                    status = if (uid == 0) ShizukuStatus.CONNECTED_ROOT else ShizukuStatus.CONNECTED_ADB,
                    shizukuVersion = safeGetVersion(),
                    uid = uid,
                )
                return
            }
        }

        bindShellService()
    }

    fun requestPermissionOrBind() {
        val binderAlive = try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }
        if (!binderAlive) {
            refreshStatus()
            return
        }

        val granted = try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }

        if (!granted) {
            try {
                if (!Shizuku.isPreV11()) {
                    Shizuku.requestPermission(REQUEST_CODE_SHIZUKU)
                }
            } catch (_: Throwable) {
            }
        } else {
            bindShellService()
        }
    }

    private fun bindShellService() {
        try {
            _state.value = _state.value.copy(
                status = ShizukuStatus.CONNECTING,
                shizukuVersion = safeGetVersion(),
            )
            Shizuku.bindUserService(userServiceArgs, serviceConnection)
        } catch (_: Throwable) {
            _state.value = _state.value.copy(status = ShizukuStatus.DISCONNECTED)
        }
    }

    private fun safeGetVersion(): Int? {
        return try {
            if (Shizuku.pingBinder()) Shizuku.getVersion() else null
        } catch (_: Throwable) {
            null
        }
    }

    private fun checkLocalSu() {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/data/adb/ksu/bin/su",
            "/data/adb/ap/bin/su",
        )
        val exists = paths.any { java.io.File(it).exists() }
        _state.value = _state.value.copy(hasLocalRoot = exists)
    }

    suspend fun execPrivileged(command: String): ShellResult = withContext(Dispatchers.IO) {
        val service = shellService
        if (service != null) {
            try {
                val raw = service.exec(command)
                val parsed = parseServiceOutput(raw)
                val mode = if (_state.value.status == ShizukuStatus.CONNECTED_ROOT) {
                    "Shizuku Root"
                } else {
                    "Shizuku ADB"
                }
                return@withContext parsed.copy(privilegeMode = mode)
            } catch (_: Throwable) {
                shellService = null
                isBound = false
            }
        }

        if (_state.value.hasLocalRoot) {
            val suResult = execLocalProcess(arrayOf("su", "-c", command), "Root (su)")
            if (suResult.exitCode == 0 || !suResult.output.contains("Permission denied", true)) {
                return@withContext suResult
            }
        }

        execLocalProcess(arrayOf("/system/bin/sh", "-c", command), "App Sandbox")
    }

    suspend fun execReadOnly(command: String): ShellResult = withContext(Dispatchers.IO) {
        val service = shellService
        if (service != null) {
            try {
                val raw = service.exec(command)
                return@withContext parseServiceOutput(raw).copy(privilegeMode = "Shizuku ADB")
            } catch (_: Throwable) {
            }
        }
        execLocalProcess(arrayOf("/system/bin/sh", "-c", command), "Local Shell")
    }

    private fun parseServiceOutput(raw: String): ShellResult {
        val firstNewline = raw.indexOf('\n')
        if (raw.startsWith("__EXIT__:") && firstNewline > 0) {
            val code = raw.substring(9, firstNewline).trim().toIntOrNull() ?: 1
            val body = raw.substring(firstNewline + 1).trimEnd()
            return ShellResult(code, body, "Shizuku")
        }
        return ShellResult(0, raw.trimEnd(), "Shizuku")
    }

    private fun execLocalProcess(cmd: Array<String>, mode: String): ShellResult {
        return try {
            val process = ProcessBuilder(*cmd)
                .redirectErrorStream(true)
                .start()
            val sb = StringBuilder()
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line: String?
                var count = 0
                while (reader.readLine().also { line = it } != null && count < 3000) {
                    sb.append(line).append('\n')
                    count++
                }
            }
            val finished = process.waitFor(15, TimeUnit.SECONDS)
            val exit = if (finished) process.exitValue() else {
                process.destroyForcibly()
                124
            }
            ShellResult(exit, sb.toString().trimEnd(), mode)
        } catch (t: Throwable) {
            ShellResult(1, t.message ?: "Execution failed", mode)
        }
    }
}
