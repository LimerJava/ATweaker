package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class UpdateCheckResult(
    val isUpdateAvailable: Boolean,
    val currentLabel: String,
    val availableLabel: String?,
    val source: String,
)

class FotaUpdateChecker(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()
) {
    suspend fun checkForUpdate(deviceInfo: DeviceInfo): UpdateCheckResult = withContext(Dispatchers.IO) {
        val profile = deviceInfo.activeProfile
        val modelForFota = if (deviceInfo.isProfileOverridden || !deviceInfo.isPhysicalSamsungA) {
            "${profile.modelPrefixes.first()}B"
        } else {
            deviceInfo.rawModel
        }
        val csc = deviceInfo.cscCode.ifBlank { profile.defaultCsc }

        val fotaBuild = fetchFotaLatestVersion(csc, modelForFota)
        val currentAndroidMajor = deviceInfo.androidVersion.substringBefore('.').toIntOrNull() ?: 14

        // If FOTA returned a newer build string or our physical/overridden Android version is behind latest official
        if (fotaBuild != null &&
            deviceInfo.isPhysicalSamsungA &&
            !deviceInfo.isProfileOverridden &&
            !fotaBuild.equals(deviceInfo.buildIncremental, ignoreCase = true)
        ) {
            val fotaClean = fotaBuild.substringBefore('/')
            return@withContext UpdateCheckResult(
                isUpdateAvailable = true,
                currentLabel = "Android ${deviceInfo.androidVersion}",
                availableLabel = "$fotaClean (${profile.latestOneUiVersion})",
                source = "Samsung FOTA ($csc)",
            )
        }

        if (currentAndroidMajor < profile.latestAndroidVersion) {
            return@withContext UpdateCheckResult(
                isUpdateAvailable = true,
                currentLabel = "Android ${deviceInfo.androidVersion}",
                availableLabel = "Android ${profile.latestAndroidVersion} • ${profile.latestOneUiVersion}",
                source = if (fotaBuild != null) "Samsung FOTA ($csc)" else "Galaxy A Firmware DB",
            )
        }

        UpdateCheckResult(
            isUpdateAvailable = false,
            currentLabel = "Android ${deviceInfo.androidVersion}",
            availableLabel = fotaBuild?.substringBefore('/'),
            source = if (fotaBuild != null) "Samsung FOTA ($csc)" else "Local Check",
        )
    }

    private fun fetchFotaLatestVersion(csc: String, model: String): String? {
        return try {
            val url = "https://fota-cloud-dn.ospserver.net/firmware/$csc/$model/version.xml"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", "Kies2.0_FUS")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val xml = resp.body?.string() ?: return null
                val matcher = Pattern.compile("<latest[^>]*>([^<]+)</latest>").matcher(xml)
                if (matcher.find()) matcher.group(1)?.trim() else null
            }
        } catch (_: Throwable) {
            null
        }
    }
}
