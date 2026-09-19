package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("study_ai_prefs", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(getLanguage())
    val language: StateFlow<String> = _language.asStateFlow()

    private val _theme = MutableStateFlow(getTheme())
    val theme: StateFlow<String> = _theme.asStateFlow()

    private val _currentUserId = MutableStateFlow(getCurrentUserId())
    val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(isOnboardingCompleted())
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(isNotificationsEnabled())
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun getLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, "ar") ?: "ar"
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _language.value = lang
    }

    fun getTheme(): String {
        return prefs.getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
        _theme.value = theme
    }

    fun getCurrentUserId(): Long {
        return prefs.getLong(KEY_CURRENT_USER_ID, -1L)
    }

    fun setCurrentUserId(userId: Long) {
        prefs.edit().putLong(KEY_CURRENT_USER_ID, userId).apply()
        _currentUserId.value = userId
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        _isOnboardingCompleted.value = completed
    }

    fun isNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        _notificationsEnabled.value = enabled
    }

    companion object {
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_THEME = "key_theme"
        private const val KEY_CURRENT_USER_ID = "key_current_user_id"
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
        private const val KEY_NOTIFICATIONS_ENABLED = "key_notifications_enabled"

        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
    }
}
