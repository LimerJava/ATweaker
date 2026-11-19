package com.example.terminal

import com.example.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class FileEntry(
    val name: String,
    val fullPath: String,
    val isDirectory: Boolean,
    val permissions: String,
    val sizeLabel: String,
    val owner: String,
    val isSymlink: Boolean = false,
)

class RootFileManager(private val shizukuManager: ShizukuManager) {

    suspend fun listDirectory(path: String): Pair<List<FileEntry>, String> = withContext(Dispatchers.IO) {
        val normalized = normalizePath(path)
        val escaped = escapeSingleQuotes(normalized)
        val cmd = "ls -la '$escaped' 2>&1"
        val res = shizukuManager.execPrivileged(cmd)

        val parsed = parseLsOutput(normalized, res.output)
        if (parsed.isNotEmpty()) {
            return@withContext parsed to res.privilegeMode
        }

        // Fallback to Java File API if local directory is directly readable
        val dir = File(normalized)
        val children = dir.listFiles()
        if (children != null) {
            val entries = children.map { f ->
                FileEntry(
                    name = f.name,
                    fullPath = f.absolutePath,
                    isDirectory = f.isDirectory,
                    permissions = buildString {
                        append(if (f.isDirectory) 'd' else '-')
                        append(if (f.canRead()) 'r' else '-')
                        append(if (f.canWrite()) 'w' else '-')
                        append(if (f.canExecute()) 'x' else '-')
                        append("------")
                    },
                    sizeLabel = if (f.isDirectory) "DIR" else formatSize(f.length()),
                    owner = "app",
                )
            }.sortedWith(compareByDescending<FileEntry> { it.isDirectory }.thenBy { it.name.lowercase() })
            return@withContext entries to "Java File API"
        }

        emptyList<FileEntry>() to res.privilegeMode
    }

    suspend fun readFilePreview(path: String): String = withContext(Dispatchers.IO) {
        val escaped = escapeSingleQuotes(path)
        val res = shizukuManager.execPrivileged("head -c 16384 '$escaped' 2>&1")
        if (res.output.isNotBlank()) return@withContext res.output
        try {
            File(path).takeIf { it.exists() && it.canRead() }?.readText()?.take(16384)
                ?: "(Empty or permission denied)"
        } catch (t: Throwable) {
            t.message ?: "Cannot read file"
        }
    }

    suspend fun createDirectory(parentPath: String, name: String): Boolean = withContext(Dispatchers.IO) {
        val target = "${parentPath.trimEnd('/')}/${name.trim()}"
        val res = shizukuManager.execPrivileged("mkdir -p '${escapeSingleQuotes(target)}'")
        if (res.isSuccess) return@withContext true
        File(target).mkdirs()
    }

    suspend fun deletePath(path: String): Boolean = withContext(Dispatchers.IO) {
        val res = shizukuManager.execPrivileged("rm -rf '${escapeSingleQuotes(path)}'")
        if (res.isSuccess) return@withContext true
        File(path).deleteRecursively()
    }

    private fun parseLsOutput(parentPath: String, output: String): List<FileEntry> {
        val entries = mutableListOf<FileEntry>()
        val base = parentPath.trimEnd('/')
        val lines = output.lineSequence()
        for (raw in lines) {
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("total ")) continue
            // Standard toybox/toolbox ls -la format:
            // drwxr-xr-x 2 root root 4096 2025-01-01 12:00 name
            val tokens = line.split(Regex("\\s+"), limit = 8)
            if (tokens.size < 7) continue
            val perms = tokens[0]
            if (perms.length < 4 || (perms[0] !in listOf('d', '-', 'l', 'c', 'b', 'p', 's'))) continue
            val owner = "${tokens[2]}:${tokens[3]}"
            val sizeOrDate = tokens[4]
            val rawName = if (tokens.size >= 8) tokens[7] else tokens.last()
            val cleanName = rawName.substringBefore(" -> ").trim()
            if (cleanName == "." || cleanName == "..") continue
            val isDir = perms.startsWith('d') || (perms.startsWith('l') && !cleanName.contains('.'))
            val sizeLabel = if (isDir) "DIR" else sizeOrDate.toLongOrNull()?.let { formatSize(it) } ?: sizeOrDate
            entries.add(
                FileEntry(
                    name = cleanName,
                    fullPath = if (base.isEmpty()) "/$cleanName" else "$base/$cleanName",
                    isDirectory = isDir,
                    permissions = perms,
                    sizeLabel = sizeLabel,
                    owner = owner,
                    isSymlink = perms.startsWith('l'),
                )
            )
        }
        return entries.sortedWith(compareByDescending<FileEntry> { it.isDirectory }.thenBy { it.name.lowercase() })
    }

    private fun normalizePath(path: String): String {
        val clean = path.trim()
        return if (clean.isEmpty()) "/" else clean
    }

    private fun escapeSingleQuotes(s: String): String = s.replace("'", "'\"'\"'")

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.1f MB", bytes / (1024f * 1024f))
        }
    }
}
