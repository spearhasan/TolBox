package com.example.core.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val title: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("toolbox_preferences", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _autoSaveMedia = MutableStateFlow(loadAutoSaveMedia())
    val autoSaveMedia: StateFlow<Boolean> = _autoSaveMedia.asStateFlow()

    private fun loadThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    private fun loadAutoSaveMedia(): Boolean {
        // Default true: automatically saves completed media/documents in mobile storage
        return prefs.getBoolean(KEY_AUTO_SAVE_MEDIA, true)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setAutoSaveMedia(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SAVE_MEDIA, enabled).apply()
        _autoSaveMedia.value = enabled
    }

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_AUTO_SAVE_MEDIA = "pref_auto_save_media"
    }
}
