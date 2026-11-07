package com.davinder.subguard.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {

    private val auth = FirebaseAuth.getInstance()

    suspend fun refreshAndCheckVerified(): Boolean {
        auth.currentUser?.reload()?.await()
        return auth.currentUser?.isEmailVerified ?: false
    }

    suspend fun sendVerificationEmail() {
        auth.currentUser?.sendEmailVerification()?.await()
    }

    fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Sign-in failed") }
    }

    fun registerWithEmail(
        email: String,
        password: String,
        displayName: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val updates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                result.user?.updateProfile(updates)
                    ?.addOnSuccessListener {
                        result.user?.sendEmailVerification()
                        onSuccess()
                    }
                    ?.addOnFailureListener { onFailure(it.message ?: "Profile update failed") }
            }
            .addOnFailureListener { onFailure(it.message ?: "Registration failed") }
    }

    fun firebaseAuthWithGoogle(
        idToken: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun sendPasswordReset(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to send reset email") }
    }

    fun isSignedIn(): Boolean = auth.currentUser != null

    fun signOut() = auth.signOut()
}