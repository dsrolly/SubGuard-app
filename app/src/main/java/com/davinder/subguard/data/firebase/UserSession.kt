package com.davinder.subguard.data.firebase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SessionUser(
    val isSignedIn: Boolean = false,
    val displayName: String = "",
    val email: String = ""
)

/**
 * Lightweight observable "who is signed in" holder, so the top bar avatar updates
 * after login / logout / profile edit. Call refresh() after changing the Firebase user's name.
 */
object UserSession {
    private val auth = FirebaseAuth.getInstance()
    private val _user = MutableStateFlow(read())
    val user: StateFlow<SessionUser> = _user.asStateFlow()

    init {
        auth.addAuthStateListener { refresh() }
    }

    fun refresh() {
        _user.value = read()
    }

    private fun read(): SessionUser {
        val u = auth.currentUser
        return SessionUser(
            isSignedIn = u != null,
            displayName = u?.displayName.orEmpty(),
            email = u?.email.orEmpty()
        )
    }
}