package com.davinder.subguard.ui.screens.profilesetup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davinder.subguard.data.local.GuestSessionMigration
import com.davinder.subguard.data.model.UserProfile
import com.davinder.subguard.data.repository.UserProfileRepository
import com.davinder.subguard.util.ProfileFormState
import com.davinder.subguard.util.formatDobIso
import com.davinder.subguard.util.validateProfileForm
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

data class ProfileSetupUiState(
    val form: ProfileFormState = ProfileFormState(),
    val isSaving: Boolean = false,
    val error: String? = null
)

class ProfileSetupViewModel : ViewModel() {

    private val repository = UserProfileRepository()

    var uiState by mutableStateOf(ProfileSetupUiState())
        private set

    fun onFormChange(form: ProfileFormState) {
        uiState = uiState.copy(form = form, error = null)
    }

    fun submit(onComplete: () -> Unit) {
        if (uiState.isSaving) return
        val form = uiState.form
        // Name was already collected at Register (or comes from Google), so it's not required here.
        val error = validateProfileForm(form, requireName = false)
        if (error != null) {
            uiState = uiState.copy(error = error)
            return
        }
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            uiState = uiState.copy(error = "You're signed out. Please log in again.")
            return
        }

        uiState = uiState.copy(isSaving = true, error = null)
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                repository.saveProfile(
                    UserProfile(
                        uid = user.uid,
                        phoneNumber = form.phoneStored,
                        email = user.email.orEmpty(),
                        dateOfBirth = formatDobIso(form.dobMillis!!),
                        gender = form.gender!!,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            } catch (e: Exception) {
                uiState = uiState.copy(isSaving = false, error = e.message ?: "Couldn't save profile. Try again.")
                return@launch
            }
            // Carry over anything added while browsing as a guest in this session.
            // A failure here must not block the user from entering the app.
            try {
                GuestSessionMigration().migrateIfAny()
            } catch (_: Exception) {
            }
            uiState = uiState.copy(isSaving = false)
            onComplete()
        }
    }
}