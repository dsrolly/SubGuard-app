package com.davinder.subguard.ui.screens.emailverification

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davinder.subguard.data.firebase.FirebaseAuthManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class EmailVerificationUiState(
    val email: String = "",
    val isChecking: Boolean = false,
    val cooldownSeconds: Int = 0,
    val info: String? = null,
    val error: String? = null
)

class EmailVerificationViewModel : ViewModel() {

    private val authManager = FirebaseAuthManager()
    private var cooldownJob: Job? = null

    var uiState by mutableStateOf(
        EmailVerificationUiState(email = FirebaseAuth.getInstance().currentUser?.email.orEmpty())
    )
        private set

    init {
        // Register already fired the first email, so start with a short cooldown.
        startCooldown()
    }

    fun resend() {
        if (uiState.cooldownSeconds > 0) return
        viewModelScope.launch {
            try {
                authManager.sendVerificationEmail()
                uiState = uiState.copy(info = "Verification email sent", error = null)
                startCooldown()
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "Couldn't send email. Try again.", info = null)
            }
        }
    }

    fun checkVerified(onVerified: () -> Unit) {
        if (uiState.isChecking) return
        uiState = uiState.copy(isChecking = true, error = null, info = null)
        viewModelScope.launch {
            val verified = try {
                authManager.refreshAndCheckVerified()
            } catch (e: Exception) {
                uiState = uiState.copy(isChecking = false, error = "Couldn't check right now. Check your connection.")
                return@launch
            }
            if (verified) {
                uiState = uiState.copy(isChecking = false)
                onVerified()
            } else {
                uiState = uiState.copy(
                    isChecking = false,
                    error = "Not verified yet. Open the link in your email, then try again."
                )
            }
        }
    }

    fun signOut(onDone: () -> Unit) {
        FirebaseAuth.getInstance().signOut()
        onDone()
    }

    private fun startCooldown(seconds: Int = 30) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (s in seconds downTo 1) {
                uiState = uiState.copy(cooldownSeconds = s)
                delay(1000)
            }
            uiState = uiState.copy(cooldownSeconds = 0)
        }
    }
}