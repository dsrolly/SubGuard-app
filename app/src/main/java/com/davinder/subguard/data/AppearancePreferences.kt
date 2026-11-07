package com.davinder.subguard.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

object AppearancePreferences {
    private const val PREF_NAME = "subguard_appearance_prefs"
    private const val KEY_COLOR_ID = "key_color_id"
    private const val KEY_WIDGET_OPACITY = "key_widget_opacity"
    private const val KEY_WIDGET_SPEND = "key_widget_spend"
    private const val KEY_WIDGET_ALERTS = "key_widget_alerts"

    private lateinit var prefs: SharedPreferences

    val themeColorMap = mapOf(
        "azure" to Color(0xFF0284C7),
        "rose" to Color(0xFFF43F5E),
        "amber" to Color(0xFFF59E0B),
        "emerald" to Color(0xFF10B981),
        "violet" to Color(0xFF8B5CF6),
        "teal" to Color(0xFF14B8A6),
        "indigo" to Color(0xFF6366F1),
        "coral" to Color(0xFFFF6B6B),
        "black" to Color(0xFF0F172A)
    )

    val isDarkModeState = mutableStateOf(false)
    val currentAccentState = mutableStateOf(Color(0xFF0284C7))

    val widgetOpacityState = mutableFloatStateOf(1.0f)
    val widgetSpendState = mutableStateOf(true)
    val widgetAlertsState = mutableStateOf(true)

    fun init(context: Context) {
        if (!::prefs.isInitialized) {
            prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val savedColorId = prefs.getString(KEY_COLOR_ID, "azure") ?: "azure"
            currentAccentState.value = themeColorMap[savedColorId] ?: Color(0xFF0284C7)
            widgetOpacityState.floatValue = prefs.getFloat(KEY_WIDGET_OPACITY, 1.0f)
            widgetSpendState.value = prefs.getBoolean(KEY_WIDGET_SPEND, true)
            widgetAlertsState.value = prefs.getBoolean(KEY_WIDGET_ALERTS, true)
        }
    }

    fun getSavedColorId(): String =
        if (::prefs.isInitialized) prefs.getString(KEY_COLOR_ID, "azure") ?: "azure" else "azure"

    fun updateDarkMode(isDark: Boolean) {
        isDarkModeState.value = isDark
    }

    fun saveColorId(id: String) {
        currentAccentState.value = themeColorMap[id] ?: Color(0xFF0284C7)
        if (::prefs.isInitialized) {
            prefs.edit().putString(KEY_COLOR_ID, id).apply()
        }
    }

    fun saveWidgetSettings(opacity: Float, showSpend: Boolean, showAlerts: Boolean) {
        widgetOpacityState.floatValue = opacity
        widgetSpendState.value = showSpend
        widgetAlertsState.value = showAlerts
        if (::prefs.isInitialized) {
            prefs.edit()
                .putFloat(KEY_WIDGET_OPACITY, opacity)
                .putBoolean(KEY_WIDGET_SPEND, showSpend)
                .putBoolean(KEY_WIDGET_ALERTS, showAlerts)
                .apply()
        }
    }
}