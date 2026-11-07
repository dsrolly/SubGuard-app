package com.davinder.subguard.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.davinder.subguard.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.prefsDataStore by preferencesDataStore(name = "subguard_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_GUEST_MODE = booleanPreferencesKey("guest_mode")
        private val KEY_ONBOARDING_SEEN = booleanPreferencesKey("onboarding_seen")
        private val KEY_MAIN_CURRENCY = stringPreferencesKey("main_currency")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_NOTIF_ENABLED = booleanPreferencesKey("notif_enabled")
        private val KEY_BILLING_REMINDER_DAYS = intPreferencesKey("billing_reminder_days")
        private val KEY_TRIAL_REMINDER_DAYS = intPreferencesKey("trial_reminder_days")
        private val KEY_GROUP_DAILY = booleanPreferencesKey("group_daily_reminders")
        private val KEY_BILLING_REMINDERS = booleanPreferencesKey("billing_reminders")
        private val KEY_TRIAL_REMINDERS = booleanPreferencesKey("trial_reminders")
        private val KEY_HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        private val KEY_CUSTOM_TAGS = stringSetPreferencesKey("custom_tags")

        // Which "days before renewal" reminders the user wants (default 7, 3, 1)
        private val KEY_REMINDER_OFFSETS = stringSetPreferencesKey("reminder_offsets")

        // Dashboard display settings
        private val KEY_SPENDING_VIEW = stringPreferencesKey("dashboard_spending_view")
        private val KEY_CASH_FLOW_VIEW = booleanPreferencesKey("dashboard_cash_flow_view")
        private val KEY_CYCLE_START_DAY = intPreferencesKey("dashboard_cycle_start_day")

        val ALL_OFFSETS = listOf(7, 3, 1, 0)
    }

    val guestMode: Flow<Boolean> = context.prefsDataStore.data
        .map { it[KEY_GUEST_MODE] ?: false }

    val onboardingSeen: Flow<Boolean> = context.prefsDataStore.data
        .map { it[KEY_ONBOARDING_SEEN] ?: false }

    val mainCurrency: Flow<String> = context.prefsDataStore.data
        .map { it[KEY_MAIN_CURRENCY] ?: "INR" }

    val themeMode: Flow<ThemeMode> = context.prefsDataStore.data
        .map { prefs ->
            when (prefs[KEY_THEME_MODE]) {
                "LIGHT" -> ThemeMode.LIGHT
                "DARK" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }

    val notificationsEnabled: Flow<Boolean> = context.prefsDataStore.data
        .map { it[KEY_NOTIF_ENABLED] ?: true }

    val billingReminderDays: Flow<Int> = context.prefsDataStore.data
        .map { it[KEY_BILLING_REMINDER_DAYS] ?: 3 }

    val trialReminderDays: Flow<Int> = context.prefsDataStore.data
        .map { it[KEY_TRIAL_REMINDER_DAYS] ?: 2 }

    val groupDailyReminders: Flow<Boolean> = context.prefsDataStore.data
        .map { it[KEY_GROUP_DAILY] ?: false }

    val billingReminders: Flow<Boolean> = context.prefsDataStore.data
        .map { it[KEY_BILLING_REMINDERS] ?: true }

    val trialReminders: Flow<Boolean> = context.prefsDataStore.data
        .map { it[KEY_TRIAL_REMINDERS] ?: true }

    // Used by ReminderWorker. Default = 7, 3 and 1 day before.
    val reminderOffsets: Flow<Set<Int>> = context.prefsDataStore.data
        .map { prefs ->
            prefs[KEY_REMINDER_OFFSETS]
                ?.mapNotNull { it.toIntOrNull() }
                ?.toSet()
                ?: setOf(7, 3, 1)
        }
    val hapticEnabled: Flow<Boolean> = context.prefsDataStore.data
        .map { it[KEY_HAPTIC_ENABLED] ?: true }

    val customTags: Flow<Set<String>> = context.prefsDataStore.data
        .map { it[KEY_CUSTOM_TAGS] ?: emptySet() }

    /** "DAILY", "WEEKLY" or "MONTHLY" */
    val spendingView: Flow<String> = context.prefsDataStore.data
        .map { it[KEY_SPENDING_VIEW] ?: "MONTHLY" }

    val cashFlowView: Flow<Boolean> = context.prefsDataStore.data
        .map { it[KEY_CASH_FLOW_VIEW] ?: false }

    /** 1..28 */
    val cycleStartDay: Flow<Int> = context.prefsDataStore.data
        .map { it[KEY_CYCLE_START_DAY] ?: 1 }

    suspend fun setGuestMode(value: Boolean) =
        context.prefsDataStore.edit { it[KEY_GUEST_MODE] = value }

    suspend fun setOnboardingSeen(value: Boolean) =
        context.prefsDataStore.edit { it[KEY_ONBOARDING_SEEN] = value }

    suspend fun setMainCurrency(value: String) =
        context.prefsDataStore.edit { it[KEY_MAIN_CURRENCY] = value }

    suspend fun setThemeMode(value: ThemeMode) =
        context.prefsDataStore.edit { it[KEY_THEME_MODE] = value.name }

    suspend fun setNotificationsEnabled(value: Boolean) =
        context.prefsDataStore.edit { it[KEY_NOTIF_ENABLED] = value }

    suspend fun setBillingReminderDays(value: Int) =
        context.prefsDataStore.edit { it[KEY_BILLING_REMINDER_DAYS] = value }

    suspend fun setTrialReminderDays(value: Int) =
        context.prefsDataStore.edit { it[KEY_TRIAL_REMINDER_DAYS] = value }

    suspend fun setGroupDailyReminders(value: Boolean) =
        context.prefsDataStore.edit { it[KEY_GROUP_DAILY] = value }

    suspend fun setBillingReminders(value: Boolean) =
        context.prefsDataStore.edit { it[KEY_BILLING_REMINDERS] = value }

    suspend fun setTrialReminders(value: Boolean) =
        context.prefsDataStore.edit { it[KEY_TRIAL_REMINDERS] = value }

    suspend fun setReminderOffsets(value: Set<Int>) =
        context.prefsDataStore.edit { prefs ->
            prefs[KEY_REMINDER_OFFSETS] = value.map { it.toString() }.toSet()
        }

    suspend fun setSpendingView(value: String) =
        context.prefsDataStore.edit { it[KEY_SPENDING_VIEW] = value }

    suspend fun setCashFlowView(value: Boolean) =
        context.prefsDataStore.edit { it[KEY_CASH_FLOW_VIEW] = value }

    suspend fun setCycleStartDay(value: Int) =
        context.prefsDataStore.edit { it[KEY_CYCLE_START_DAY] = value.coerceIn(1, 28) }

    suspend fun setHapticEnabled(value: Boolean) =
        context.prefsDataStore.edit { it[KEY_HAPTIC_ENABLED] = value }

    suspend fun addCustomTag(name: String) =
        context.prefsDataStore.edit { prefs ->
            prefs[KEY_CUSTOM_TAGS] = (prefs[KEY_CUSTOM_TAGS] ?: emptySet()) + name
        }

    suspend fun removeCustomTag(name: String) =
        context.prefsDataStore.edit { prefs ->
            prefs[KEY_CUSTOM_TAGS] = (prefs[KEY_CUSTOM_TAGS] ?: emptySet())
                .filterNot { it.equals(name, ignoreCase = true) }
                .toSet()
        }
}