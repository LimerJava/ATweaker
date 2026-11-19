package com.example.data

import android.os.Build
import android.system.Os
import java.io.BufferedReader
import java.io.InputStreamReader

data class DeviceInfo(
    val manufacturer: String,
    val rawModel: String,
    val deviceCodename: String,
    val androidVersion: String,
    val sdkInt: Int,
    val buildIncremental: String,
    val oneUiVersion: String?,
    val cscCode: String,
    val kernelVersion: String,
    val arch: String,
    val socHardware: String,
    val matchedProfile: GalaxyAModelProfile?,
    val activeProfile: GalaxyAModelProfile,
    val isPhysicalSamsungA: Boolean,
    val isProfileOverridden: Boolean,
)

object DeviceInspector {

    fun inspect(overrideProfileId: String? = null): DeviceInfo {
        val manufacturer = Build.MANUFACTURER.ifBlank { "Samsung" }
        val rawModel = Build.MODEL.ifBlank { "Unknown" }
        val deviceCodename = Build.DEVICE.ifBlank { "unknown" }
        val androidRelease = Build.VERSION.RELEASE.ifBlank { "14" }
        val sdkInt = Build.VERSION.SDK_INT
        val buildIncremental = Build.VERSION.INCREMENTAL.ifBlank { "unknown" }
        val arch = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        val socHardware = listOfNotNull(
            getSystemProp("ro.soc.model").takeIf { it.isNotBlank() },
            getSystemProp("ro.hardware.chipname").takeIf { it.isNotBlank() },
            Build.HARDWARE.takeIf { it.isNotBlank() },
        ).firstOrNull() ?: "Unknown SoC"

        val oneUiProp = getSystemProp("ro.build.version.oneui").toIntOrNull()
        val oneUiFormatted = oneUiProp?.let { code ->
            val major = code / 10000
            val minor = (code % 10000) / 100
            "One UI $major.$minor"
        }

        val csc = getSystemProp("ro.csc.sales_code")
            .ifBlank { getSystemProp("persist.sys.csc.sales_code") }
            .ifBlank { "SER" }

        val kernel = try {
            Os.uname().release
        } catch (_: Throwable) {
            "Linux"
        }

        val matched = SamsungDeviceCatalog.findByModelCode(rawModel)
        val overridden = SamsungDeviceCatalog.findById(overrideProfileId)

        val fallbackProfile = matched ?: GalaxyAModelProfile(
            id = "GENERIC_A",
            seriesNumber = 55,
            marketingName = if (manufacturer.contains("samsung", true)) {
                "Samsung $rawModel"
            } else {
                "$rawModel (A-Series Profile)"
            },
            modelPrefixes = listOf(rawModel),
            socName = socHardware,
            socVendor = detectSocVendor(socHardware),
            arch = arch,
            kernelBranch = "Linux ${kernel.substringBefore('-')}",
            displayType = DisplayPanelType.SUPER_AMOLED,
            maxRefreshRateHz = 120,
            supports5G = true,
            isOneUiCore = false,
            supportsEnhancedProcessing = true,
            supportsRamPlus = true,
            latestAndroidVersion = (androidRelease.substringBefore('.').toIntOrNull() ?: 14) + 1,
            latestOneUiVersion = oneUiFormatted ?: "One UI 7.0",
            defaultCsc = csc,
        )

        val active = overridden ?: fallbackProfile

        return DeviceInfo(
            manufacturer = manufacturer,
            rawModel = rawModel,
            deviceCodename = deviceCodename,
            androidVersion = androidRelease,
            sdkInt = sdkInt,
            buildIncremental = buildIncremental,
            oneUiVersion = oneUiFormatted,
            cscCode = csc,
            kernelVersion = kernel,
            arch = arch,
            socHardware = socHardware,
            matchedProfile = matched,
            activeProfile = active,
            isPhysicalSamsungA = matched != null,
            isProfileOverridden = overridden != null,
        )
    }

    private fun detectSocVendor(soc: String): SocVendor {
        val lower = soc.lowercase()
        return when {
            lower.contains("exynos") || lower.contains("s5e") -> SocVendor.EXYNOS
            lower.contains("qcom") || lower.contains("snapdragon") || lower.contains("sm") -> SocVendor.SNAPDRAGON
            lower.contains("mt") || lower.contains("helio") || lower.contains("dimensity") -> SocVendor.MEDIATEK
            lower.contains("unisoc") || lower.contains("ums") -> SocVendor.UNISOC
            else -> SocVendor.OTHER
        }
    }

    fun getSystemProp(key: String): String {
        return try {
            val process = ProcessBuilder("/system/bin/getprop", key).start()
            BufferedReader(InputStreamReader(process.inputStream)).use {
                it.readLine()?.trim().orEmpty()
            }
        } catch (_: Throwable) {
            ""
        }
    }
}
