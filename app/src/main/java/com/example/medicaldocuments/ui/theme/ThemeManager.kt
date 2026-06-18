package com.example.medicaldocuments.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "app_theme"

    private lateinit var prefs: SharedPreferences

    private val _currentTheme = MutableStateFlow(AppTheme.SYSTEM)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedTheme = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name)
        _currentTheme.value = savedTheme?.let {
            try {
                AppTheme.valueOf(it)
            } catch (e: Exception) {
                AppTheme.SYSTEM
            }
        } ?: AppTheme.SYSTEM
    }

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }

    fun getCurrentTheme(): AppTheme = _currentTheme.value
}