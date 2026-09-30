package com.shakeexpense.app.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class ThemePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("shakeexpense_theme_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _isShakeEnabled = MutableStateFlow(loadShakeEnabled())
    val isShakeEnabled: StateFlow<Boolean> = _isShakeEnabled.asStateFlow()

    private val _isShakeAnywhereEnabled = MutableStateFlow(loadShakeAnywhereEnabled())
    val isShakeAnywhereEnabled: StateFlow<Boolean> = _isShakeAnywhereEnabled.asStateFlow()

    private fun loadThemeMode(): AppThemeMode {
        val savedName = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        return try {
            AppThemeMode.valueOf(savedName ?: AppThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    private fun loadShakeEnabled(): Boolean {
        return prefs.getBoolean(KEY_SHAKE_ENABLED, true)
    }

    private fun loadShakeAnywhereEnabled(): Boolean {
        return prefs.getBoolean(KEY_SHAKE_ANYWHERE_ENABLED, true)
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setShakeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHAKE_ENABLED, enabled).apply()
        _isShakeEnabled.value = enabled
    }

    fun setShakeAnywhereEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHAKE_ANYWHERE_ENABLED, enabled).apply()
        _isShakeAnywhereEnabled.value = enabled
    }

    companion object {
        private const val KEY_THEME_MODE = "key_app_theme_mode"
        private const val KEY_SHAKE_ENABLED = "key_shake_enabled"
        private const val KEY_SHAKE_ANYWHERE_ENABLED = "key_shake_anywhere_enabled"

        @Volatile
        private var INSTANCE: ThemePreferences? = null

        fun getInstance(context: Context): ThemePreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemePreferences(context).also { INSTANCE = it }
            }
        }

        fun isShakeEnabled(context: Context): Boolean {
            val prefs = context.applicationContext.getSharedPreferences("shakeexpense_theme_prefs", Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_SHAKE_ENABLED, true)
        }

        fun isShakeAnywhereEnabled(context: Context): Boolean {
            val prefs = context.applicationContext.getSharedPreferences("shakeexpense_theme_prefs", Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_SHAKE_ANYWHERE_ENABLED, true)
        }
    }
}
