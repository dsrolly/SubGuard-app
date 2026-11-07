package com.davinder.subguard.ui.screens.editprofile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davinder.subguard.data.firebase.UserSession
import com.davinder.subguard.data.model.UserProfile
import com.davinder.subguard.data.repository.UserProfileRepository
import com.davinder.subguard.util.DEFAULT_DISPLAY_NAME
import com.davinder.subguard.util.GENDER_OPTIONS
import com.davinder.subguard.util.ProfileFormState
import com.davinder.subguard.util.formatDobIso
import com.davinder.subguard.util.parseDobIso
import com.davinder.subguard.util.parsePhone
import com.davinder.subguard.util.validateProfileForm
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class EditProfileUiState(
    val isLoading: Boolean = true,
    val form: ProfileFormState = ProfileFormState(),
    val email: String = "",
    val isSaving: Boolean = false,
    val loadFailed: Boolean = false,
    val error: String? = null
)

class EditProfileViewModel : ViewModel() {

    private val repository = UserProfileRepository()
    private var original = ProfileFormState()
    private var existing: UserProfile? = null

    var uiState by mutableStateOf(EditProfileUiState())
        private set

    /** True when the form differs from what was loaded/saved (drives Save enabled + discard dialog). */
    val hasChanges: Boolean
        get() = uiState.form.copy(name = uiState.form.name.trim()) != original

    init {
        load()
    }

    private fun load() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            uiState = EditProfileUiState(isLoading = false, loadFailed = true, error = "You're signed out.")
            return
        }
        viewModelScope.launch {
            val profile = try {
                repository.getProfile()
            } catch (e: Exception) {
                uiState = EditProfileUiState(
                    isLoading = false,
                    email = user.email.orEmpty(),
                    loadFailed = true,
                    error = "Couldn't load your profile. Check your connection and try again."
                )
                return@launch
            }
            existing = profile
            val (code, number) = parsePhone(profile?.phoneNumber.orEmpty())
            val name = user.displayName.orEmpty().let { if (it == DEFAULT_DISPLAY_NAME) "" else it }
            val form = ProfileFormState(
                name = name,
                countryCode = code,
                phone = number,
                dobMillis = parseDobIso(profile?.dateOfBirth.orEmpty()),
                gender = profile?.gender?.takeIf { it in GENDER_OPTIONS }
            )
            original = form
            uiState = EditProfileUiState(isLoading = false, form = form, email = user.email.orEmpty())
        }
    }

    fun onFormChange(form: ProfileFormState) {
        uiState = uiState.copy(form = form, error = null)
    }

    fun save(onSaved: () -> Unit) {
        if (uiState.isSaving || uiState.loadFailed) return
        val form = uiState.form
        val error = validateProfileForm(form, requireName = true)
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
                val name = form.name.trim()
                if (name != user.displayName) {
                    user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()
                }
                val now = System.currentTimeMillis()
                // Keep createdAt of the existing profile; a missing profile (user skipped setup) is created here.
                val profile = (existing ?: UserProfile(uid = user.uid, email = user.email.orEmpty(), createdAt = now))
                    .copy(
                        phoneNumber = form.phoneStored,
                        dateOfBirth = formatDobIso(form.dobMillis!!),
                        gender = form.gender!!,
                        updatedAt = now
                    )
                repository.saveProfile(profile)
                UserSession.refresh() // top bar avatar picks up the new name
                existing = profile
                original = form.copy(name = name)
                uiState = uiState.copy(isSaving = false, form = original)
                onSaved()
            } catch (e: Exception) {
                uiState = uiState.copy(isSaving = false, error = e.message ?: "Couldn't save. Try again.")
            }
        }
    }
}