package com.davinder.subguard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davinder.subguard.data.local.PreferencesManager
import com.davinder.subguard.data.model.BillingCycle
import com.davinder.subguard.data.model.BillingHistoryEntry
import com.davinder.subguard.data.model.Subscription
import com.davinder.subguard.data.model.SubscriptionStatus
import com.davinder.subguard.data.repository.BillingHistoryRepositoryProvider
import com.davinder.subguard.data.repository.SubscriptionRepositoryProvider
import com.davinder.subguard.notification.ReminderScheduler
import com.davinder.subguard.util.calculateNextRenewalDate
import com.davinder.subguard.util.toLocalDateOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class AddEditSubscriptionUiState(
    val name: String = "",
    val amount: String = "",
    val currency: String = "INR",
    val category: String = "Entertainment",
    val billingCycle: BillingCycle = BillingCycle.MONTHLY,
    val isTrial: Boolean = false,
    val startDate: String = "",
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val remindersEnabled: Boolean = true,
    val isEditMode: Boolean = false,
    val nameError: String? = null,
    val amountError: String? = null,
    val isSaving: Boolean = false,
    val isLoadingExisting: Boolean = false,
    val saveComplete: Boolean = false
)

/**
 * AndroidViewModel providing application context to SubscriptionRepositoryProvider
 * to resolve either Firestore or Guest mode repositories.
 */
class AddEditSubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SubscriptionRepositoryProvider.get(application)

    /** Custom tags created on the Tags & Categories screen; shown as extra category chips. */
    val customTags: StateFlow<List<String>> = PreferencesManager(application).customTags
        .map { tags -> tags.filter { it.isNotBlank() }.sortedBy { it.lowercase() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // New subscriptions default their first billing date to today.
    private val _uiState = MutableStateFlow(
        AddEditSubscriptionUiState(
            startDate = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        )
    )
    val uiState: StateFlow<AddEditSubscriptionUiState> = _uiState.asStateFlow()

    private var editingId: String? = null
    private var loadedFor: String? = null

    /**
     * Initializes the form with existing subscription data if in edit mode.
     * Safe against multiple recomposition calls.
     */
    fun loadExisting(subscriptionId: String?) {
        if (subscriptionId.isNullOrBlank() || loadedFor == subscriptionId) return
        loadedFor = subscriptionId
        _uiState.value = _uiState.value.copy(isLoadingExisting = true)

        viewModelScope.launch {
            val match = repository.observeSubscriptions().first().find { it.id == subscriptionId }
            if (match != null) {
                editingId = match.id
                _uiState.value = AddEditSubscriptionUiState(
                    name = match.name,
                    amount = match.amount.toString(),
                    currency = match.currency,
                    category = match.category,
                    billingCycle = match.billingCycle,
                    isTrial = match.isTrial,
                    startDate = match.startDate,
                    status = match.status,
                    remindersEnabled = match.remindersEnabled,
                    isEditMode = true,
                    isLoadingExisting = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoadingExisting = false)
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, nameError = null)
    }

    fun onAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(amount = value, amountError = null)
    }

    fun onCurrencyChange(value: String) {
        _uiState.value = _uiState.value.copy(currency = value)
    }

    fun onCategoryChange(value: String) {
        _uiState.value = _uiState.value.copy(category = value)
    }

    fun onBillingCycleChange(value: BillingCycle) {
        _uiState.value = _uiState.value.copy(billingCycle = value)
    }

    fun onStartDateChange(isoDate: String) {
        _uiState.value = _uiState.value.copy(startDate = isoDate)
    }

    fun onTrialToggle(value: Boolean) {
        _uiState.value = _uiState.value.copy(isTrial = value)
    }

    fun save() {
        val state = _uiState.value
        val amountValue = state.amount.toDoubleOrNull()

        val nameError = if (state.name.isBlank()) "Name can't be empty" else null
        val amountError = if (amountValue == null || amountValue <= 0) "Enter a valid amount" else null

        if (nameError != null || amountError != null) {
            _uiState.value = state.copy(nameError = nameError, amountError = amountError)
            return
        }

        val wasNew = editingId == null
        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            // Parse selected date; fallback to today if invalid
            val startDate = state.startDate.toLocalDateOrNull() ?: today
            val nextRenewal = calculateNextRenewalDate(startDate, state.billingCycle, today)

            val subscription = Subscription(
                id = editingId ?: "",
                name = state.name.trim(),
                amount = amountValue!!,
                currency = state.currency,
                category = state.category,
                billingCycle = state.billingCycle,
                isTrial = state.isTrial,
                startDate = startDate.toString(),
                nextRenewalDate = nextRenewal.toString(),
                status = state.status,
                remindersEnabled = state.remindersEnabled
            )

            val savedId = if (wasNew) {
                repository.addSubscription(subscription)
            } else {
                repository.updateSubscription(subscription)
                editingId!!
            }

            val savedSubscription = subscription.copy(id = savedId)

            // Respect existing pause/reminders-off status
            if (savedSubscription.status == SubscriptionStatus.ACTIVE && savedSubscription.remindersEnabled) {
                ReminderScheduler.scheduleReminders(getApplication(), savedSubscription)
            } else {
                ReminderScheduler.cancelReminders(getApplication(), savedId)
            }

            // Seed billing history entry only for brand-new subscriptions
            if (wasNew) {
                BillingHistoryRepositoryProvider.get(getApplication()).addEntry(
                    BillingHistoryEntry(
                        subscriptionId = savedId,
                        subscriptionName = savedSubscription.name,
                        amount = savedSubscription.amount,
                        currency = savedSubscription.currency,
                        category = savedSubscription.category,
                        billedOn = savedSubscription.startDate
                    )
                )
            }

            _uiState.value = _uiState.value.copy(isSaving = false, saveComplete = true)
        }
    }
}