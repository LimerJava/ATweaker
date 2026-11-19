package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "atweaker_settings")

enum class AppThemeMode {
    DARK,
    LIGHT,
    AMOLED_BLACK,
    NIGHT_VIOLET,
}

enum class AccentColorPreset {
    DYNAMIC,
    SAMSUNG_COBALT,
    ELECTRIC_CYAN,
    NEON_VIOLET,
    EMERALD_MINT,
    CRIMSON_CORAL,
    AMBER_GOLD,
}

enum class AppLanguage {
    RU,
    EN,
}

data class UserSettings(
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val accentPreset: AccentColorPreset = AccentColorPreset.ELECTRIC_CYAN,
    val language: AppLanguage = AppLanguage.RU,
    val showDangerousTweaks: Boolean = false,
    val overrideProfileId: String? = null,
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_PRESET = stringPreferencesKey("accent_preset")
        val LANGUAGE = stringPreferencesKey("language")
        val SHOW_DANGEROUS = booleanPreferencesKey("show_dangerous_tweaks")
        val OVERRIDE_PROFILE = stringPreferencesKey("override_profile_id")
    }

    val settingsFlow: Flow<UserSettings> = context.appDataStore.data.map { prefs ->
        val theme = prefs[Keys.THEME_MODE]?.let { name ->
            AppThemeMode.entries.firstOrNull { it.name == name }
        } ?: AppThemeMode.DARK
        val accent = prefs[Keys.ACCENT_PRESET]?.let { name ->
            AccentColorPreset.entries.firstOrNull { it.name == name }
        } ?: AccentColorPreset.ELECTRIC_CYAN
        val lang = prefs[Keys.LANGUAGE]?.let { name ->
            AppLanguage.entries.firstOrNull { it.name == name }
        } ?: AppLanguage.RU
        val dangerous = prefs[Keys.SHOW_DANGEROUS] ?: false
        val overrideId = prefs[Keys.OVERRIDE_PROFILE]?.takeIf { it.isNotBlank() }
        UserSettings(
            themeMode = theme,
            accentPreset = accent,
            language = lang,
            showDangerousTweaks = dangerous,
            overrideProfileId = overrideId,
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.appDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setAccentPreset(preset: AccentColorPreset) {
        context.appDataStore.edit { it[Keys.ACCENT_PRESET] = preset.name }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.appDataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setShowDangerousTweaks(show: Boolean) {
        context.appDataStore.edit { it[Keys.SHOW_DANGEROUS] = show }
    }

    suspend fun setOverrideProfileId(profileId: String?) {
        context.appDataStore.edit {
            if (profileId.isNullOrBlank()) {
                it.remove(Keys.OVERRIDE_PROFILE)
            } else {
                it[Keys.OVERRIDE_PROFILE] = profileId
            }
        }
    }
}
