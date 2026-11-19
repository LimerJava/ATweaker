package com.example.model

import com.example.data.DisplayPanelType
import com.example.data.GalaxyAModelProfile
import com.example.data.SocVendor

enum class TweakCategory {
    DISPLAY,
    PERFORMANCE,
    BATTERY,
    ONE_UI,
    NETWORK,
    CAMERA_AUDIO,
    DANGEROUS,
}

enum class TweakControlType {
    TOGGLE,
    CHOICE,
    ACTION,
}

data class TweakOption(
    val id: String,
    val labelRu: String,
    val labelEn: String,
    val command: String,
    val expectedReadValue: String? = null,
)

data class TweakCompatibility(
    val minSdk: Int = 24,
    val maxSdk: Int = 99,
    val requireAmoled: Boolean = false,
    val minRefreshRateHz: Int = 60,
    val require5G: Boolean = false,
    val excludeOneUiCore: Boolean = false,
    val requireRamPlus: Boolean = false,
    val requireEnhancedProcessing: Boolean = false,
    val allowedSocVendors: Set<SocVendor>? = null,
    val minSeriesNumber: Int = 1,
    val maxSeriesNumber: Int = 57,
) {
    fun matches(profile: GalaxyAModelProfile, sdkInt: Int): Boolean {
        if (sdkInt < minSdk || sdkInt > maxSdk) return false
        if (profile.seriesNumber < minSeriesNumber || profile.seriesNumber > maxSeriesNumber) return false
        if (requireAmoled && profile.displayType != DisplayPanelType.SUPER_AMOLED) return false
        if (profile.maxRefreshRateHz < minRefreshRateHz) return false
        if (require5G && !profile.supports5G) return false
        if (excludeOneUiCore && profile.isOneUiCore) return false
        if (requireRamPlus && !profile.supportsRamPlus) return false
        if (requireEnhancedProcessing && !profile.supportsEnhancedProcessing) return false
        if (allowedSocVendors != null && profile.socVendor !in allowedSocVendors) return false
        return true
    }
}

data class TweakItem(
    val id: String,
    val category: TweakCategory,
    val titleRu: String,
    val titleEn: String,
    val descriptionRu: String,
    val descriptionEn: String,
    val whyHiddenRu: String,
    val whyHiddenEn: String,
    val controlType: TweakControlType,
    val readCommand: String? = null,
    val enableCommand: String? = null,
    val disableCommand: String? = null,
    val expectedEnabledValue: String? = null,
    val options: List<TweakOption> = emptyList(),
    val actionCommand: String? = null,
    val isDangerous: Boolean = false,
    val synonyms: List<String> = emptyList(),
    val compatibility: TweakCompatibility = TweakCompatibility(),
)

data class TweakExecutionLog(
    val tweakId: String,
    val title: String,
    val command: String,
    val output: String,
    val success: Boolean,
    val privilegeMode: String,
    val timestampMs: Long = System.currentTimeMillis(),
)
