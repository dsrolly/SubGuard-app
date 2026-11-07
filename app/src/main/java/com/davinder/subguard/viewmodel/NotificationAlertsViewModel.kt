package com.davinder.subguard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davinder.subguard.data.local.PreferencesManager
import com.davinder.subguard.data.model.SubscriptionStatus
import com.davinder.subguard.data.repository.SubscriptionRepositoryProvider
import com.davinder.subguard.util.daysUntil
import com.davinder.subguard.util.toLocalDateOrNull
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class NotificationAlertsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SubscriptionRepositoryProvider.get(application)
    private val preferences = PreferencesManager(application)

    val alerts: StateFlow<List<UpcomingRenewal>> = combine(
        repository.observeSubscriptions(),
        preferences.reminderOffsets,
        preferences.notificationsEnabled
    ) { subscriptions, offsets, enabled ->
        val window = offsets.maxOrNull()
        if (!enabled || window == null) {
            emptyList<UpcomingRenewal>()
        } else {
            subscriptions
                .filter { it.status == SubscriptionStatus.ACTIVE && it.remindersEnabled }
                .mapNotNull { sub ->
                    val date = sub.nextRenewalDate.toLocalDateOrNull() ?: return@mapNotNull null
                    val days = daysUntil(date)
                    if (days in 0..window) UpcomingRenewal(sub, days) else null
                }
                .sortedBy { it.daysAway }
        }
    }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}