package com.davinder.subguard.ui.screens.signin

import android.app.Activity.RESULT_OK
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davinder.subguard.data.firebase.FirebaseAuthManager
import com.davinder.subguard.data.local.GuestMigration
import com.davinder.subguard.data.local.PreferencesManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember { FirebaseAuthManager(context) }
    val preferences = remember { PreferencesManager(context) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    isLoading = true
                    authManager.firebaseAuthWithGoogle(
                        idToken = idToken,
                        onSuccess = {
                            scope.launch {
                                // A guest who signs in keeps their data: it's uploaded to the account first.
                                if (preferences.guestMode.first()) {
                                    if (GuestMigration.migrateToCloud(context)) {
                                        preferences.setGuestMode(false)
                                    }
                                }
                                isLoading = false
                                onSignInSuccess()
                            }
                        },
                        onFailure = { e ->
                            isLoading = false
                            errorMessage = e.message ?: "Sign-in failed"
                        }
                    )
                }
            } catch (e: ApiException) {
                errorMessage = "Sign-in failed (code ${e.statusCode})"
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to SubGuard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Track every subscription in one place",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { launcher.launch(authManager.getSignInIntent()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Signing in..." else "Continue with Google")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = {
                    scope.launch {
                        preferences.setGuestMode(true) // remembered, so Splash skips this screen next time
                        onContinueAsGuest()
                    }
                },
                enabled = !isLoading
            ) {
                Text("Continue as guest")
            }

            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}