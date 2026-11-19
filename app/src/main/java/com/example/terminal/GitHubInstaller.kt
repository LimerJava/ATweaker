package com.example.terminal

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class GitHubReleaseAsset(
    val name: String,
    val sizeBytes: Long,
    val downloadUrl: String,
    val contentType: String,
)

data class GitHubReleaseInfo(
    val repoFullName: String,
    val tagName: String,
    val releaseName: String,
    val body: String,
    val assets: List<GitHubReleaseAsset>,
)

data class CuratedGitHubApp(
    val repo: String,
    val title: String,
    val descRu: String,
    val descEn: String,
)

class GitHubInstaller(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {
    val curatedApps = listOf(
        CuratedGitHubApp("RikkaApps/Shizuku", "Shizuku", "ADB/Root API-сервис для системных твиков", "ADB/Root privilege manager"),
        CuratedGitHubApp("ImranR98/Obtainium", "Obtainium", "Автообновление APK напрямую с GitHub", "Track & update GitHub APKs"),
        CuratedGitHubApp("termux/termux-app", "Termux", "Полноценный эмулятор терминала Linux", "Full Linux terminal emulator"),
        CuratedGitHubApp("M66B/NetGuard", "NetGuard", "Фаервол без Root для блокировки трафика", "No-root Android firewall"),
        CuratedGitHubApp("SamouraiDev/Android-Debloater", "UAD Next Gen", "Инструменты очистки системных пакетов", "System package debloater"),
    )

    fun parseRepoSlug(input: String): String? {
        val trimmed = input.trim()
            .removePrefix("https://github.com/")
            .removePrefix("http://github.com/")
            .removePrefix("github.com/")
            .trim('/')
        val parts = trimmed.split('/')
        if (parts.size >= 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
            return "${parts[0]}/${parts[1].removeSuffix(".git")}"
        }
        return null
    }

    fun fetchLatestRelease(repoInput: String): Result<GitHubReleaseInfo> {
        val slug = parseRepoSlug(repoInput)
            ?: return Result.failure(IllegalArgumentException("Invalid format. Use owner/repo or GitHub URL"))

        val urls = listOf(
            "https://api.github.com/repos/$slug/releases/latest",
            "https://api.github.com/repos/$slug/releases?per_page=1",
        )

        var lastError = "Release not found"
        for (url in urls) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "A-Tweaker-Android")
                    .build()
                client.newCall(request).execute().use { resp ->
                    val body = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) {
                        lastError = "GitHub HTTP ${resp.code}"
                        return@use
                    }
                    val jsonObj = if (body.trimStart().startsWith("[")) {
                        val arr = JSONArray(body)
                        if (arr.length() == 0) {
                            lastError = "No releases published"
                            return@use
                        }
                        arr.getJSONObject(0)
                    } else {
                        JSONObject(body)
                    }
                    val assetsJson = jsonObj.optJSONArray("assets") ?: JSONArray()
                    val assets = buildList {
                        for (i in 0 until assetsJson.length()) {
                            val a = assetsJson.getJSONObject(i)
                            add(
                                GitHubReleaseAsset(
                                    name = a.optString("name"),
                                    sizeBytes = a.optLong("size", 0L),
                                    downloadUrl = a.optString("browser_download_url"),
                                    contentType = a.optString("content_type"),
                                )
                            )
                        }
                    }
                    return Result.success(
                        GitHubReleaseInfo(
                            repoFullName = slug,
                            tagName = jsonObj.optString("tag_name", "latest"),
                            releaseName = jsonObj.optString("name").ifBlank { jsonObj.optString("tag_name", "Release") },
                            body = jsonObj.optString("body").take(600),
                            assets = assets,
                        )
                    )
                }
            } catch (t: Throwable) {
                lastError = t.message ?: "Network error"
            }
        }
        return Result.failure(IllegalStateException(lastError))
    }

    fun downloadAsset(
        asset: GitHubReleaseAsset,
        targetDir: File,
        onProgress: (Float) -> Unit,
    ): Result<File> {
        return try {
            if (!targetDir.exists()) targetDir.mkdirs()
            val outFile = File(targetDir, asset.name.replace(Regex("[^a-zA-Z0-9._-]"), "_"))
            val request = Request.Builder()
                .url(asset.downloadUrl)
                .header("User-Agent", "A-Tweaker-Android")
                .build()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    return Result.failure(IllegalStateException("Download failed: HTTP ${resp.code}"))
                }
                val body = resp.body ?: return Result.failure(IllegalStateException("Empty response"))
                val total = body.contentLength().takeIf { it > 0 } ?: asset.sizeBytes.coerceAtLeast(1L)
                body.byteStream().use { input ->
                    FileOutputStream(outFile).use { output ->
                        val buffer = ByteArray(8192)
                        var copied = 0L
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            copied += read
                            onProgress((copied.toFloat() / total.toFloat()).coerceIn(0f, 1f))
                        }
                    }
                }
            }
            Result.success(outFile)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
