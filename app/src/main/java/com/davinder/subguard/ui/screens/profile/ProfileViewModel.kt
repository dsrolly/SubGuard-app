package com.davinder.subguard.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davinder.subguard.data.firebase.FirebaseAuthManager
import com.davinder.subguard.data.firebase.UserSession
import com.davinder.subguard.data.repository.UserProfileRepository
import com.davinder.subguard.util.ageFromDobMillis
import com.davinder.subguard.util.calculateProfileCompletion
import com.davinder.subguard.util.formatDobForDisplay
import com.davinder.subguard.util.parseDobIso
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isGuest: Boolean = false,
    val displayName: String = "",
    val email: String = "",
    val emailVerified: Boolean = false,
    val phone: String = "",
    val dobDisplay: String = "",
    val gender: String = "",
    val memberSince: String = "",
    val completionPercent: Int = 0,
    val missingFields: List<String> = emptyList(),
    val canChangePassword: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {

    private val authManager = FirebaseAuthManager()
    private val repository = UserProfileRepository()

    var uiState by mutableStateOf(ProfileUiState())
        private set

    fun load() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            uiState = ProfileUiState(isLoading = false, isGuest = true)
            return
        }
        viewModelScope.launch {
            val profile = try {
                repository.getProfile()
            } catch (e: Exception) {
                null // offline / no profile yet: show what we know from the auth user
            }
            val displayName = user.displayName.orEmpty()
            val dobMillis = parseDobIso(profile?.dateOfBirth.orEmpty())
            val completion = calculateProfileCompletion(
                displayName = displayName,
                phone = profile?.phoneNumber.orEmpty(),
                dob = profile?.dateOfBirth.orEmpty(),
                gender = profile?.gender.orEmpty(),
                emailVerified = user.isEmailVerified
            )
            val createdMillis = profile?.createdAt ?: user.metadata?.creationTimestamp
            uiState = ProfileUiState(
                isLoading = false,
                isGuest = false,
                displayName = displayName,
                email = user.email.orEmpty(),
                emailVerified = user.isEmailVerified,
                phone = profile?.phoneNumber.orEmpty(),
                dobDisplay = dobMillis?.let { "${formatDobForDisplay(it)} · ${ageFromDobMillis(it)} yrs" }.orEmpty(),
                gender = profile?.gender.orEmpty(),
                memberSince = createdMillis?.let {
                    SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(it))
                }.orEmpty(),
                completionPercent = completion.percent,
                missingFields = completion.missing,
                canChangePassword = user.providerData.any { it.providerId == "password" }
            )
        }
    }

    fun sendPasswordReset() {
        val email = uiState.email
        if (email.isBlank()) return
        authManager.sendPasswordReset(
            email = email,
            onSuccess = { uiState = uiState.copy(message = "Password reset link sent to $email", error = null) },
            onFailure = { msg -> uiState = uiState.copy(error = msg, message = null) }
        )
    }

    fun signOut(onDone: () -> Unit) {
        authManager.signOut()
        UserSession.refresh()
        onDone()
    }
}