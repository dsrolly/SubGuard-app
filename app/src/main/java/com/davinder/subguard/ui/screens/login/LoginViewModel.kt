package com.davinder.subguard.ui.screens.login

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davinder.subguard.data.firebase.FirebaseAuthManager
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val info: String? = null
)

class LoginViewModel : ViewModel() {

    private val authManager = FirebaseAuthManager()

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, error = null, info = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, error = null, info = null)
    }

    fun login(onLoginSuccess: () -> Unit, onNeedsVerification: () -> Unit) {
        if (uiState.isLoading) return
        val email = uiState.email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            uiState = uiState.copy(error = "Enter a valid email address")
            return
        }
        if (uiState.password.isEmpty()) {
            uiState = uiState.copy(error = "Enter your password")
            return
        }
        uiState = uiState.copy(isLoading = true, error = null, info = null)
        viewModelScope.launch {
            val failure = signIn(email, uiState.password)
            if (failure != null) {
                uiState = uiState.copy(isLoading = false, error = failure)
                return@launch
            }
            // isEmailVerified is only a local cache, so reload from the server first.
            val verified = try {
                authManager.refreshAndCheckVerified()
            } catch (e: Exception) {
                false
            }
            uiState = uiState.copy(isLoading = false)
            if (verified) onLoginSuccess() else onNeedsVerification()
        }
    }

    fun forgotPassword() {
        val email = uiState.email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            uiState = uiState.copy(error = "Enter your email above first, then tap Forgot password")
            return
        }
        authManager.sendPasswordReset(
            email = email,
            onSuccess = {
                uiState = uiState.copy(error = null, info = "Password reset link sent to $email")
            },
            onFailure = { message ->
                uiState = uiState.copy(info = null, error = message)
            }
        )
    }

    /** Returns null on success, or an error message. */
    private suspend fun signIn(email: String, password: String): String? =
        suspendCoroutine { cont ->
            authManager.signInWithEmail(
                email = email,
                password = password,
                onSuccess = { cont.resume(null) },
                onFailure = { message -> cont.resume(message) }
            )
        }
}