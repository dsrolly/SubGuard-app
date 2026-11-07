package com.davinder.subguard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davinder.subguard.data.firebase.FirebaseAuthManager
import com.davinder.subguard.data.local.PreferencesManager
import com.davinder.subguard.data.repository.SubscriptionRepositoryProvider
import com.davinder.subguard.ui.theme.ThemeMode
import com.davinder.subguard.util.subscriptionsToCsv
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class SettingsUiState(
    val isGuest: Boolean = true,
    val userEmail: String? = null,
    val userName: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val mainCurrency: String = "INR",
    val hapticEnabled: Boolean = true,
    // Sirf email+password users ke liye true (Google users ka password nahi hota)
    val canChangePassword: Boolean = false
)

/** Result of "Change password" (reset link) request. Dono null = kuch dikhana nahi. */
data class PasswordResetState(
    val message: String? = null,
    val error: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)
    private val auth = FirebaseAuthManager(application)
    private val subscriptionRepository = SubscriptionRepositoryProvider.get(application)

    private val _passwordReset = MutableStateFlow(PasswordResetState())
    val passwordReset: StateFlow<PasswordResetState> = _passwordReset.asStateFlow()

    private fun buildState(theme: ThemeMode, currency: String, haptic: Boolean): SettingsUiState {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return SettingsUiState(
            isGuest = currentUser == null,
            userEmail = currentUser?.email,
            userName = currentUser?.displayName,
            themeMode = theme,
            mainCurrency = currency,
            hapticEnabled = haptic,
            canChangePassword = currentUser?.providerData?.any { it.providerId == "password" } == true
        )
    }

    // The initial value is read from DataStore right away (a tiny local read), so the
    // Haptic switch never flashes "ON" for a moment before showing the saved "OFF".
    private val initialState: SettingsUiState = runBlocking {
        buildState(
            prefs.themeMode.first(),
            prefs.mainCurrency.first(),
            prefs.hapticEnabled.first()
        )
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        prefs.themeMode,
        prefs.mainCurrency,
        prefs.hapticEnabled
    ) { theme, currency, haptic ->
        buildState(theme, currency, haptic)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        initialState
    )

    fun onThemeChange(mode: ThemeMode) {
        viewModelScope.launch {
            prefs.setThemeMode(mode)
        }
    }

    fun onMainCurrencyChange(currency: String) {
        viewModelScope.launch {
            prefs.setMainCurrency(currency)
        }
    }

    fun onHapticChange(enabled: Boolean) {
        viewModelScope.launch { prefs.setHapticEnabled(enabled) }
    }

    /** Signed-in user ke email par Firebase password reset link bhejta hai. */
    fun sendPasswordReset() {
        val email = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
        if (email.isBlank()) {
            _passwordReset.value = PasswordResetState(error = "No email found for this account")
            return
        }
        auth.sendPasswordReset(
            email = email,
            onSuccess = {
                _passwordReset.value = PasswordResetState(message = "Password reset link sent to $email")
            },
            onFailure = { msg ->
                _passwordReset.value = PasswordResetState(error = msg)
            }
        )
    }

    /** Toast dikhane ke baad result clear karta hai. */
    fun clearPasswordResetResult() {
        _passwordReset.value = PasswordResetState()
    }

    suspend fun buildCsv(): String {
        val subscriptions = subscriptionRepository.observeSubscriptions().first()
        return subscriptionsToCsv(subscriptions)
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                auth.signOut()
                prefs.setGuestMode(false)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                onDone()
            }
        }
    }
}