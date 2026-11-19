package com.example.data

enum class SocVendor { EXYNOS, SNAPDRAGON, MEDIATEK, UNISOC, OTHER }

enum class DisplayPanelType { SUPER_AMOLED, PLS_LCD, TFT_LCD }

data class GalaxyAModelProfile(
    val id: String,
    val seriesNumber: Int,
    val marketingName: String,
    val modelPrefixes: List<String>,
    val socName: String,
    val socVendor: SocVendor,
    val arch: String,
    val kernelBranch: String,
    val displayType: DisplayPanelType,
    val maxRefreshRateHz: Int,
    val supports5G: Boolean,
    val isOneUiCore: Boolean,
    val supportsEnhancedProcessing: Boolean,
    val supportsRamPlus: Boolean,
    val latestAndroidVersion: Int,
    val latestOneUiVersion: String,
    val defaultCsc: String = "SER",
)

object SamsungDeviceCatalog {

    val profiles: List<GalaxyAModelProfile> = listOf(
        // A0x series
        GalaxyAModelProfile("A01", 1, "Galaxy A01", listOf("SM-A015"), "Snapdragon 439", SocVendor.SNAPDRAGON, "armeabi-v7a", "Linux 4.9", DisplayPanelType.PLS_LCD, 60, false, false, false, false, 12, "One UI 4.1"),
        GalaxyAModelProfile("A01_CORE", 1, "Galaxy A01 Core", listOf("SM-A013"), "MediaTek MT6739", SocVendor.MEDIATEK, "armeabi-v7a", "Linux 4.9", DisplayPanelType.PLS_LCD, 60, false, true, false, false, 10, "One UI Core 2.0"),
        GalaxyAModelProfile("A02", 2, "Galaxy A02", listOf("SM-A022"), "MediaTek MT6739W", SocVendor.MEDIATEK, "armeabi-v7a", "Linux 4.14", DisplayPanelType.PLS_LCD, 60, false, true, false, false, 11, "One UI Core 3.1"),
        GalaxyAModelProfile("A02S", 2, "Galaxy A02s", listOf("SM-A025"), "Snapdragon 450", SocVendor.SNAPDRAGON, "arm64-v8a", "Linux 4.9", DisplayPanelType.PLS_LCD, 60, false, true, false, false, 12, "One UI Core 4.1"),
        GalaxyAModelProfile("A03", 3, "Galaxy A03", listOf("SM-A035"), "Unisoc T606", SocVendor.UNISOC, "arm64-v8a", "Linux 4.14", DisplayPanelType.PLS_LCD, 60, false, true, false, true, 13, "One UI Core 5.1"),
        GalaxyAModelProfile("A03_CORE", 3, "Galaxy A03 Core", listOf("SM-A032"), "Unisoc SC9863A", SocVendor.UNISOC, "arm64-v8a", "Linux 4.14", DisplayPanelType.PLS_LCD, 60, false, true, false, false, 11, "One UI Core 3.1"),
        GalaxyAModelProfile("A03S", 3, "Galaxy A03s", listOf("SM-A037"), "MediaTek Helio P35", SocVendor.MEDIATEK, "arm64-v8a", "Linux 4.19", DisplayPanelType.PLS_LCD, 60, false, true, false, true, 13, "One UI Core 5.1"),
        GalaxyAModelProfile("A04", 4, "Galaxy A04", listOf("SM-A045"), "MediaTek Helio P35", SocVendor.MEDIATEK, "arm64-v8a", "Linux 4.19", DisplayPanelType.PLS_LCD, 60, false, true, false, true, 14, "One UI Core 6.1"),
        GalaxyAModelProfile("A04E", 4, "Galaxy A04e", listOf("SM-A042"), "MediaTek Helio P35", SocVendor.MEDIATEK, "arm64-v8a", "Linux 4.19", DisplayPanelType.PLS_LCD, 60, false, true, false, true, 14, "One UI Core 6.1"),
        GalaxyAModelProfile("A04S", 4, "Galaxy A04s", listOf("SM-A047"), "Exynos 850", SocVendor.EXYNOS, "arm64-v8a", "Linux 4.19", DisplayPanelType.PLS_LCD, 90, false, true, false, true, 14, "One UI Core 6.1"),
        GalaxyAModelProfile("A05", 5, "Galaxy A05", listOf("SM-A055"), "MediaTek Helio G85", SocVendor.MEDIATEK, "arm64-v8a", "Linux 5.10", DisplayPanelType.PLS_LCD, 60, false, false, false, true, 15, "One UI 7.0"),
        GalaxyAModelProfile("A05S", 5, "Galaxy A05s", listOf("SM-A057"), "Snapdragon 680", SocVendor.SNAPDRAGON, "arm64-v8a", "Linux 5.10", DisplayPanelType.PLS_LCD, 90, false, false, false, true, 15, "One UI 7.0"),
        GalaxyAModelProfile("A06", 6, "Galaxy A06", listOf("SM-A065", "SM-A066"), "MediaTek Helio G85 / Dimensity 6300", SocVendor.MEDIATEK, "arm64-v8a", "Linux 5.15", DisplayPanelType.PLS_LCD, 90, false, false, false, true, 16, "One UI 8.0"),

        // A1x series
        GalaxyAModelProfile("A10", 10, "Galaxy A10", listOf("SM-A105"), "Exynos 7884", SocVendor.EXYNOS, "armeabi-v7a", "Linux 4.4", DisplayPanelType.PLS_LCD, 60, false, false, false, false, 11, "One UI 3.1"),
        GalaxyAModelProfile("A10S", 10, "Galaxy A10s", listOf("SM-A107"), "MediaTek Helio P22", SocVendor.MEDIATEK, "armeabi-v7a", "Linux 4.9", DisplayPanelType.PLS_LCD, 60, false, true, false, false, 11, "One UI Core 3.1"),
        GalaxyAModelProfile("A11", 11, "Galaxy A11", listOf("SM-A115"), "Snapdragon 450", SocVendor.SNAPDRAGON, "arm64-v8a", "Linux 4.9", DisplayPanelType.PLS_LCD, 60, false, false, false, false, 12, "One UI 4.1"),
        GalaxyAModelProfile("A12", 12, "Galaxy A12", listOf("SM-A125", "SM-A127"), "Helio P35 / Exynos 850", SocVendor.EXYNOS, "arm64-v8a", "Linux 4.19", DisplayPanelType.PLS_LCD, 60, false, true, false, true, 13, "One UI Core 5.1"),
        GalaxyAModelProfile("A13", 13, "Galaxy A13", listOf("SM-A135", "SM-A136", "SM-A137"), "Exynos 850 / Dimensity 700", SocVendor.EXYNOS, "arm64-v8a", "Linux 4.19", DisplayPanelType.PLS_LCD, 90, true, true, false, true, 14, "One UI 6.1"),
        GalaxyAModelProfile("A14", 14, "Galaxy A14", listOf("SM-A145", "SM-A146"), "Exynos 1330 / Helio G80", SocVendor.EXYNOS, "arm64-v8a", "Linux 5.10", DisplayPanelType.PLS_LCD, 90, true, true, false, true, 15, "One UI 7.0"),
        GalaxyAModelProfile("A15", 15, "Galaxy A15", listOf("SM-A155", "SM-A156"), "Helio G99 / Dimensity 6100+", SocVendor.MEDIATEK, "arm64-v8a", "Linux 5.15", DisplayPanelType.SUPER_AMOLED, 90, true, false, true, true, 16, "One UI 8.0"),
        GalaxyAModelProfile("A16", 16, "Galaxy A16", listOf("SM-A165", "SM-A166"), "Exynos 1330 / Helio G99", SocVendor.EXYNOS, "arm64-v8a", "Linux 5.15", DisplayPanelType.SUPER_AMOLED, 90, true, false, true, true, 16, "One UI 8.0"),

        // A2x series
        GalaxyAModelProfile("A20", 20, "Galaxy A20", listOf("SM-A205", "SM-A202"), "Exynos 7884", SocVendor.EXYNOS, "arm64-v8a", "Linux 4.4", DisplayPanelType.SUPER_AMOLED, 60, false, false, false, false, 11, "One UI 3.1"),
        GalaxyAModelProfile("A20S", 20, "Galaxy A20s", listOf("SM-A207"), "Snapdragon 450", SocVendor.SNAPDRAGON, "arm64-v8a", "Linux 4.9", DisplayPanelType.TFT_LCD, 60, false, true, false, false, 11, "One UI Core 3.1"),
        GalaxyAModelProfile("A21", 21, "Galaxy A21", listOf("SM-A215"), "MediaTek Helio P35", SocVendor.MEDIATEK, "arm64-v8a", "Linux 4.14", DisplayPanelType.TFT_LCD, 60, false, true, false, false, 12, "One UI Core 4.1"),
        GalaxyAModelProfile("A21S", 21, "Galaxy A21s", listOf("SM-A217"), "Exynos 850", SocVendor.EXYNOS, "arm64-v8a", "Linux 4.19", DisplayPanelType.PLS_LCD, 60, false, true, false, true, 12, "One UI Core 4.1"),
        GalaxyAModelProfile("A22", 22, "Galaxy A22", listOf("SM-A225", "SM-A226"), "Helio G80 / Dimensity 700", SocVendor.MEDIATEK, "arm64-v8a", "Linux 4.19", DisplayPanelType.SUPER_AMOLED, 90, true, true, false, true, 13, "One UI Core 5.1"),
        GalaxyAModelProfile("A23", 23, "Galaxy A23", listOf("SM-A235", "SM-A236"), "Snapdragon 680 / 695 5G", SocVendor.SNAPDRAGON, "arm64-v8a", "Linux 5.4", DisplayPanelType.PLS_LCD, 120, true, false, true, true, 14, "One UI 6.1"),
        GalaxyAModelProfile("A24", 24, "Galaxy A24", listOf("SM-A245"), "MediaTek Helio G99", SocVendor.MEDIATEK, "arm64-v8a", "Linux 5.10", DisplayPanelType.SUPER_AMOLED, 90, false, false, true, true, 16, "One UI 8.0"),
        GalaxyAModelProfile("A25", 25, "Galaxy A25 5G", listOf("SM-A256"), "Exynos 1280", SocVendor.EXYNOS, "arm64-v8a", "Linux 5.10", DisplayPanelType.SUPER_AMOLED, 120, true, false, true, true, 16, "One UI 8.0"),
        GalaxyAModelProfile("A26", 26, "Galaxy A26 5G", listOf("SM-A266"), "Exynos 1380", SocVendor.EXYNOS, "arm64-v8a", "Linux 5.15", DisplayPanelType.SUPER_AMOLED, 120, true, false, true, true, 16, "One UI 8.0"),

        // A3x series
        GalaxyAModelProfile("A30", 30, "Galaxy A30", listOf("SM-A305"), "Exynos 7904", SocVendor.EXYNOS, "arm64-v8a", "Linux 4.4", DisplayPanelType.SUPER_AMOLED, 60, false, false, false, false, 11, "One UI 3.1"),
        GalaxyAModelProfile("A30S", 30, "Galaxy A30s", listOf("SM-A307"), "Exynos 7904", SocVendor.EXYNOS, "arm64-v8a", "Linux 4.4", DisplayPanelType.SUPER_AMOLED, 60, false, false, false, false, 11, "One UI 3.1"),
        GalaxyAModelProfile("A31", 31, "Galaxy A31", listOf("SM-A315"), "MediaTek Helio P65", SocVendor.MEDIATEK, "arm64-v8a", "Linux 4.14", DisplayPanelType.SUPER_AMOLED, 60, false, false, false, false, 12, "One UI 4.1"),
        GalaxyAModelProfile("A32", 32, "Galaxy A32", listOf("SM-A325", "SM-A326"), "Helio G80 / Dimensity 720", SocVendor.MEDIATEK, "arm64-v8a", "Linux 4.19", DisplayPanelType.SUPER_AMOLED, 90, true, false, true, true, 13, "One UI 5.1"),
        GalaxyAModelProfile("A33", 33, "Galaxy A33 5G", listOf("SM-A336"), "Exynos 1280", SocVendor.EXYNOS, "arm64-v8a", "Linux 5.10", DisplayPanelType.SUPER_AMOLED, 90, true, false, true, true, 16, "One UI 8.0"),
        GalaxyAModelProfile("A34", 34, "Galaxy A34 5G", listOf("SM-A346"), "Dimensity 1080", SocVendor.MEDIATEK, "arm64-v8a", "Linux 5.10", DisplayPanelType.SUPER_AMOLED, 120, true, false, true, true, 16, "One UI 8.0"),
        GalaxyAModelProfile("A35", 35, "Galaxy A35 5G", listOf("SM-A356"), "Exynos 1380", SocVendor.EXYNOS, "arm64-v8a", "Linux 5.15", DisplayPanelType.SUPER_AMOLED, 120, true, false, true, true, 16, "One UI 8.0"),
        GalaxyAModelProfile("A36", 36, "Galaxy A36 5G", listOf("SM-A366"), "Snapdragon 6 Gen 3", SocVendor.SNAPDRAGON, "arm64-v8a", "Linux 6.1", DisplayPanelType.SUPER_AMOLED, 120, true, false, true, true, 16, "One UI 8.0"),

        // A4x series
        GalaxyAModelProfile("A40", 40, "Galaxy A40", listOf("SM-A405"), "Exynos 7904", SocVendor.EXYNOS, "arm64-v8a", "Linux 4.4", DisplayPanelType.SUPER_AMOLED, 60, false, false, false, false, 11, "One UI 3.1"),
        GalaxyAModelProfile("A41", 41, "Galaxy A41", listOf("SM-A415"), "MediaTek Helio P65", SocVendor.MEDIATEK, "arm64-v8a", "Linux 4.14", DisplayPanelType.SUPER_AMOLED, 60, false, false, false, false, 12, "One UI 4.1"),
        GalaxyAModelProfile("A42", 42, "Galaxy A42 5G", listOf("SM-A426"), "Snapdragon 750G", SocVendor.SNAPDRAGON, "arm64-v8a", "Linux 4.19", DisplayPanelType.SUPER_AMOLED, 60, true, false, true, true, 13, "One UI 5.1"),

        // A5x series (up to A57!)
        GalaxyAModelProfile("A50", 50, "Galaxy A50", listOf("SM-A505"), "Exynos 9610", SocVendor.EXYNOS, "arm64-v8a", "Linux 4.14", DisplayPanelType.SUPER_AMOLED, 60, false, false, false, false, 11, "One UI 3.1"),
        GalaxyAModelProfile("A50S", 50, "Galaxy A50s", listOf("SM-A507"), "Exynos 9611", SocVendor.EXYNOS, "arm64-v8a", "Linux 4.14", DisplayPanelType.SUPER_AMOLED, 60, false, false, false, false, 11, "One UI 3.1"),
        GalaxyAModelProfile("A51", 51, "Galaxy A51", listOf("SM-A515", "SM-A516"), "Exynos 9611 / 980 5G", SocVendor.EXYNOS, "arm64-v8a", "Linux 4.14", DisplayPanelType.SUPER_AMOLED, 60, true, false, true, true, 13, "One UI 5.1"),
        GalaxyAModelProfile("A52", 52, "Galaxy A52", listOf("SM-A525", "SM-A526"), "Snapdragon 720G / 750G", SocVendor.SNAPDRAGON, "arm64-v8a", "Linux 4.19", DisplayPanelType.SUPER_AMOLED, 120, true, false, true, true, 14, "One UI 6.1"),
        GalaxyAModelProfile("A52S", 52, "Galaxy A52s 5G", listOf("SM-A528"), "Snapdragon 778G 5G", SocVendor.SNAPDRAGON, "arm64-v8a", "Linux 5.4", DisplayPanelType.SUPER_AMOLED, 120, true, false, true, true, 14, "One UI 6.1"),
        GalaxyAModelProfile("A53", 53, "Galaxy A53 5G", listOf("SM-A536"), "Exynos 1280", SocVendor.EXYNOS, "arm64-v8a", "Linux 5.10", DisplayPanelType.SUPER_AMOLED, 120, true, false, true, true, 16, "One UI 8.0"),
        GalaxyAModelProfile("A54", 54, "Galaxy A54 5G", listOf("SM-A546"), "Exynos 1380", SocVendor.EXYNOS, "arm64-v8a", "Linux 5.10", DisplayPanelType.SUPER_AMOLED, 120, true, false, true, true, 16, "One UI 8.0"),
        GalaxyAModelProfile("A55", 55, "Galaxy A55 5G", listOf("SM-A556"), "Exynos 1480 (Xclipse 530)", SocVendor.EXYNOS, "arm64-v8a", "Linux 6.1", DisplayPanelType.SUPER_AMOLED, 120, true, false, true, true, 16, "One UI 8.0"),
        GalaxyAModelProfile("A56", 56, "Galaxy A56 5G", listOf("SM-A566"), "Exynos 1580 (Xclipse 540)", SocVendor.EXYNOS, "arm64-v8a", "Linux 6.6", DisplayPanelType.SUPER_AMOLED, 120, true, false, true, true, 17, "One UI 8.5"),
        GalaxyAModelProfile("A57", 57, "Galaxy A57 5G", listOf("SM-A576"), "Exynos 1680 (Xclipse 550)", SocVendor.EXYNOS, "arm64-v8a", "Linux 6.6", DisplayPanelType.SUPER_AMOLED, 120, true, false, true, true, 17, "One UI 9.0"),
    )

    fun findByModelCode(rawModel: String): GalaxyAModelProfile? {
        val upper = rawModel.uppercase().trim()
        return profiles.firstOrNull { profile ->
            profile.modelPrefixes.any { prefix -> upper.startsWith(prefix) || upper.contains(prefix) }
        }
    }

    fun findById(id: String?): GalaxyAModelProfile? {
        if (id.isNullOrBlank()) return null
        return profiles.firstOrNull { it.id == id }
    }
}
