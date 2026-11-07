package com.davinder.subguard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davinder.subguard.data.model.Subscription
import com.davinder.subguard.data.model.SubscriptionStatus
import com.davinder.subguard.data.repository.BillingHistoryRepositoryProvider
import com.davinder.subguard.data.repository.SubscriptionRepositoryProvider
import com.davinder.subguard.notification.ReminderScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch

class SubscriptionDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val subscriptionRepository = SubscriptionRepositoryProvider.get(application)
    private val billingHistoryRepository = BillingHistoryRepositoryProvider.get(application)

    private val _subscription = MutableStateFlow<Subscription?>(null)
    val subscription: StateFlow<Subscription?> = _subscription.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _notFound = MutableStateFlow(false)
    val notFound: StateFlow<Boolean> = _notFound.asStateFlow()

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted.asStateFlow()

    private var observeJob: Job? = null
    private var currentSubscriptionId: String? = null

    /**
     * Continuously observes the repository flow instead of doing a one-off `.first()` read.
     * This guarantees that when returning from the Edit screen, the updated details are
     * immediately reflected without triggering a false 'not found' state.
     */
    fun load(subscriptionId: String) {
        if (subscriptionId.isBlank()) {
            _notFound.value = true
            _isLoading.value = false
            return
        }

        // Avoid re-subscribing if already observing the same subscription
        if (currentSubscriptionId == subscriptionId && observeJob?.isActive == true) {
            return
        }

        currentSubscriptionId = subscriptionId
        _isLoading.value = (_subscription.value == null)
        _notFound.value = false

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            subscriptionRepository.observeSubscriptions()
                .catch { _isLoading.value = false }
                .collect { list ->
                val match = list.find { it.id == subscriptionId }
                if (match != null) {
                    _subscription.value = match
                    _notFound.value = false
                    _isLoading.value = false
                } else {
                    // Only mark notFound if the list was loaded and the item is genuinely absent
                    if (list.isNotEmpty()) {
                        _subscription.value = null
                        _notFound.value = true
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    /**
     * Deletes the subscription, cascades deletion to associated billing history entries,
     * and cancels all scheduled notification reminders.
     */
    fun delete() {
        val sub = _subscription.value ?: return
        viewModelScope.launch {
            // Cancel active observation before deleting to prevent UI flicker
            observeJob?.cancel()

            // 1. Delete subscription record from repository
            subscriptionRepository.deleteSubscription(sub.id)

            // 2. Cascade delete all billing history records created for this subscription
            billingHistoryRepository.deleteEntriesForSubscription(sub.id)

            // 3. Cancel scheduled renewal notification alarms
            ReminderScheduler.cancelReminders(getApplication(), sub.id)

            _isDeleted.value = true
        }
    }

    /**
     * Reminders for just THIS subscription — doesn't touch global Notification settings.
     */
    fun onToggleReminders(enabled: Boolean) {
        val sub = _subscription.value ?: return
        val updated = sub.copy(remindersEnabled = enabled)
        _subscription.value = updated
        viewModelScope.launch {
            subscriptionRepository.updateSubscription(updated)
            if (enabled) {
                ReminderScheduler.scheduleReminders(getApplication(), updated)
            } else {
                ReminderScheduler.cancelReminders(getApplication(), updated.id)
            }
        }
    }

    fun onTogglePaused(paused: Boolean) {
        val sub = _subscription.value ?: return
        val newStatus = if (paused) SubscriptionStatus.PAUSED else SubscriptionStatus.ACTIVE
        val updated = sub.copy(status = newStatus)
        _subscription.value = updated
        viewModelScope.launch {
            subscriptionRepository.updateSubscription(updated)
            // A paused subscription shouldn't nag the user with renewal reminders
            if (paused) {
                ReminderScheduler.cancelReminders(getApplication(), updated.id)
            } else if (updated.remindersEnabled) {
                ReminderScheduler.scheduleReminders(getApplication(), updated)
            }
        }
    }
}