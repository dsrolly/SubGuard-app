package com.davinder.subguard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davinder.subguard.data.repository.BillingHistoryRepositoryProvider
import com.davinder.subguard.data.repository.SubscriptionRepositoryProvider
import com.davinder.subguard.notification.ReminderScheduler
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

enum class DeleteReason(val label: String) {
    NOT_USING("I don't use SubGuard anymore"),
    BETTER_APP("I found a better app"),
    NOTIFICATIONS("Too many notifications"),
    PRIVACY("Privacy or data concerns"),
    MISSING_FEATURES("Missing features I need"),
    TECHNICAL("Bugs or technical problems"),
    OTHER("Other")
}

data class DeleteAccountUiState(
    /** 0 = choose reason, 1 = verify + delete, 2 = account deleted message */
    val step: Int = 0,
    val reason: DeleteReason? = null,
    val otherText: String = "",
    val password: String = "",
    val email: String = "",
    val isPasswordUser: Boolean = true,
    val isDeleting: Boolean = false,
    val error: String? = null
)

class DeleteAccountViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<DeleteAccountUiState> = _uiState.asStateFlow()

    private fun initialState(): DeleteAccountUiState {
        val user = auth.currentUser
        return DeleteAccountUiState(
            email = user?.email.orEmpty(),
            isPasswordUser = user?.providerData?.any { it.providerId == EmailAuthProvider.PROVIDER_ID } ?: true
        )
    }

    fun onReasonSelected(reason: DeleteReason) =
        _uiState.update { it.copy(reason = reason, error = null) }

    fun onOtherTextChange(text: String) =
        _uiState.update { it.copy(otherText = text.take(200)) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, error = null) }

    fun continueToVerify() {
        if (_uiState.value.reason == null) return
        _uiState.update { it.copy(step = 1, error = null) }
    }

    fun backToReason() =
        _uiState.update { it.copy(step = 0, password = "", error = null) }

    /** Checks everything we can BEFORE the "Are you sure?" dialog. Returns true if OK to ask. */
    fun validateBeforeDelete(): Boolean {
        val state = _uiState.value
        val user = auth.currentUser
        if (user == null) {
            _uiState.update { it.copy(error = "You are not signed in") }
            return false
        }
        if (state.isPasswordUser && state.password.isBlank()) {
            _uiState.update { it.copy(error = "Enter your password to continue") }
            return false
        }
        if (!state.isPasswordUser) {
            // Google accounts have no password. Firebase only allows deletion shortly after sign-in.
            val lastSignIn = user.metadata?.lastSignInTimestamp ?: 0L
            if (System.currentTimeMillis() - lastSignIn > RECENT_LOGIN_WINDOW_MS) {
                _uiState.update {
                    it.copy(error = "For security, please sign out, sign in again with Google, and then delete your account.")
                }
                return false
            }
        }
        return true
    }

    fun deleteAccount() {
        val user = auth.currentUser ?: return
        val state = _uiState.value
        if (state.isDeleting) return
        _uiState.update { it.copy(isDeleting = true, error = null) }

        viewModelScope.launch {
            try {
                // 1. Re-verify identity (password users)
                if (state.isPasswordUser) {
                    val email = user.email ?: throw IllegalStateException("Account has no email")
                    user.reauthenticate(EmailAuthProvider.getCredential(email, state.password)).awaitTask()
                }

                // 2. Save the reason (anonymous, best effort - never blocks deletion)
                saveFeedback(state)

                // 3. Remove all of the user's data
                val finished = withTimeoutOrNull(DATA_DELETE_TIMEOUT_MS) {
                    deleteUserData(user)
                    true
                }
                if (finished == null) {
                    _uiState.update {
                        it.copy(isDeleting = false, error = "Network is slow. Please check your internet and try again.")
                    }
                    return@launch
                }

                // 4. Finally delete the sign-in account itself
                user.delete().awaitTask()

                _uiState.update { it.copy(isDeleting = false, step = 2, password = "") }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                fail("Incorrect password. Please try again.")
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                fail("For security, please sign out, sign in again, and then delete your account.")
            } catch (e: FirebaseTooManyRequestsException) {
                fail("Too many attempts. Please wait a few minutes and try again.")
            } catch (e: FirebaseNetworkException) {
                fail("No internet connection. Please try again.")
            } catch (e: Exception) {
                fail("Couldn't delete your account: ${e.message ?: "unknown error"}")
            }
        }
    }

    private fun fail(message: String) =
        _uiState.update { it.copy(isDeleting = false, error = message) }

    private suspend fun deleteUserData(user: FirebaseUser) {
        val app = getApplication<Application>()
        val subscriptionRepo = SubscriptionRepositoryProvider.get(app)
        val billingRepo = BillingHistoryRepositoryProvider.get(app)

        subscriptionRepo.observeSubscriptions().first().forEach { sub ->
            ReminderScheduler.cancelReminders(app, sub.id)
            billingRepo.deleteEntriesForSubscription(sub.id)
            subscriptionRepo.deleteSubscription(sub.id)
        }

        // Separate profile document (phone, date of birth, gender)
        FirebaseFirestore.getInstance()
            .collection("userProfiles")
            .document(user.uid)
            .delete()
            .awaitTask()
    }

    private suspend fun saveFeedback(state: DeleteAccountUiState) {
        val reason = state.reason ?: return
        try {
            withTimeoutOrNull(4_000) {
                FirebaseFirestore.getInstance()
                    .collection("accountDeletionFeedback")
                    .add(
                        mapOf(
                            "reason" to reason.name,
                            "comment" to if (reason == DeleteReason.OTHER) state.otherText.trim() else "",
                            "signInMethod" to if (state.isPasswordUser) "password" else "google",
                            "createdAt" to System.currentTimeMillis()
                        )
                    ).awaitTask()
            }
        } catch (_: Exception) {
            // Feedback is optional; ignore (for example Firestore rules not allowing it).
        }
    }

    private companion object {
        const val RECENT_LOGIN_WINDOW_MS = 4 * 60 * 1000L
        const val DATA_DELETE_TIMEOUT_MS = 25_000L
    }
}

private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result -> if (cont.isActive) cont.resume(result) }
    addOnFailureListener { error -> if (cont.isActive) cont.resumeWithException(error) }
}