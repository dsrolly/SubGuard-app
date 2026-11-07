package com.davinder.subguard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davinder.subguard.data.local.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
class NotificationSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)


    private val initialEnabled = runBlocking { prefs.notificationsEnabled.first() }
    private val initialOffsets = runBlocking { prefs.reminderOffsets.first() }

    val enabled: StateFlow<Boolean> = prefs.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialEnabled)

    val offsets: StateFlow<Set<Int>> = prefs.reminderOffsets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialOffsets)

    fun onEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setNotificationsEnabled(enabled)
        }
    }

    fun onToggleOffset(days: Int) {
        viewModelScope.launch {
            val current = offsets.value
            val updated = if (days in current) current - days else current + days
            prefs.setReminderOffsets(updated)
        }
    }
}