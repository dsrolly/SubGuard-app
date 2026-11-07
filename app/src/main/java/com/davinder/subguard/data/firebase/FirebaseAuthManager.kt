package com.davinder.subguard.data.firebase

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager(private val context: Context? = null) {

    private val auth = FirebaseAuth.getInstance()

    fun getSignInIntent(): Intent {
        val ctx = context ?: throw IllegalStateException("Context required for Google Sign-In intent")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(ctx.getString(com.davinder.subguard.R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(ctx, gso)
        return client.signInIntent
    }

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
