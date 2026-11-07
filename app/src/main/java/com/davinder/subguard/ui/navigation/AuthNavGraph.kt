package com.davinder.subguard.ui.navigation

import android.content.Context
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.davinder.subguard.data.firebase.FirebaseAuthManager
import com.davinder.subguard.data.local.PreferencesManager
import com.davinder.subguard.data.repository.UserProfileRepository
import com.davinder.subguard.ui.screens.emailverification.EmailVerificationScreen
import com.davinder.subguard.ui.screens.login.LoginScreen
import com.davinder.subguard.ui.screens.onboarding.OnboardingScreen
import com.davinder.subguard.ui.screens.profilesetup.ProfileSetupScreen
import com.davinder.subguard.ui.screens.register.RegisterScreen
import com.davinder.subguard.ui.screens.splash.SplashDestination
import com.davinder.subguard.ui.screens.splash.SplashScreen
import com.davinder.subguard.ui.screens.welcome.WelcomeScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Every pre-dashboard route: Splash, Onboarding, Welcome (Screen.SignIn), Register,
 * EmailVerification, ProfileSetup, Login.
 */
fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    enterApp: () -> Unit
) {
    fun goAndClear(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    composable(Screen.Splash.route) {
        val context = LocalContext.current
        SplashScreen(
            onNavigateToOnboarding = { goAndClear(Screen.Onboarding.route) },
            onNavigateToEmailVerification = { goAndClear(Screen.EmailVerification.route) },
            onNavigateToWelcome = { goAndClear(Screen.SignIn.route) },
            onNavigateToDashboard = enterApp,
            resolveDestination = { resolveSplashDestination(context) }
        )
    }

    composable(Screen.Onboarding.route) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        OnboardingScreen(
            onFinished = {
                scope.launch {
                    PreferencesManager(context).setOnboardingSeen(true)
                    goAndClear(Screen.SignIn.route)
                }
            }
        )
    }

    // Welcome screen
    composable(Screen.SignIn.route) {
        val scope = rememberCoroutineScope()
        WelcomeScreen(
            onNavigateToRegister = { navController.navigate(Screen.Register.route) },
            onNavigateToLogin = { navController.navigate(Screen.Login.route) },
            onGoogleSignInSuccess = {
                scope.routeAfterAuth(enterApp) { goAndClear(Screen.ProfileSetup.route) }
            },
            onContinueAsGuest = enterApp
        )
    }

    composable(Screen.Register.route) {
        RegisterScreen(
            onRegistered = {
                navController.navigate(Screen.EmailVerification.route) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                }
            }
        )
    }

    composable(Screen.EmailVerification.route) {
        EmailVerificationScreen(
            onVerified = { goAndClear(Screen.ProfileSetup.route) },
            onSignOut = { goAndClear(Screen.SignIn.route) }
        )
    }

    composable(Screen.ProfileSetup.route) {
        ProfileSetupScreen(onComplete = enterApp)
    }

    composable(Screen.Login.route) {
        val scope = rememberCoroutineScope()
        LoginScreen(
            onLoginSuccess = {
                scope.routeAfterAuth(enterApp) { goAndClear(Screen.ProfileSetup.route) }
            },
            onNeedsVerification = {
                navController.navigate(Screen.EmailVerification.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            },
            onBack = { navController.popBackStack() }
        )
    }
}

private suspend fun resolveSplashDestination(context: Context): SplashDestination {
    val onboardingSeen = PreferencesManager(context).onboardingSeen.first()

    // Rule 1: Fresh install me Onboarding slide dikhana mandatory hai
    if (!onboardingSeen) {
        // Agar system cache me koi purana token bacha bhi ho, use clean karein
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {}
        return SplashDestination.ONBOARDING
    }

    // Rule 2: Agar onboarding ho chuki hai par user sign in nahi hai
    val user = FirebaseAuth.getInstance().currentUser ?: return SplashDestination.WELCOME

    // Rule 3: Verification check
    val verified = try {
        FirebaseAuthManager().refreshAndCheckVerified()
    } catch (e: Exception) {
        user.isEmailVerified
    }

    return if (verified) SplashDestination.DASHBOARD else SplashDestination.EMAIL_VERIFICATION
}

private fun CoroutineScope.routeAfterAuth(
    enterApp: () -> Unit,
    goToProfileSetup: () -> Unit
) = launch {
    val hasProfile = try {
        UserProfileRepository().hasProfile()
    } catch (e: Exception) {
        false
    }
    if (hasProfile) enterApp() else goToProfileSetup()
}