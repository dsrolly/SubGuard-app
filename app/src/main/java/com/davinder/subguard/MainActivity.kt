package com.davinder.subguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.davinder.subguard.data.local.PreferencesManager
import com.davinder.subguard.ui.navigation.Screen
import com.davinder.subguard.ui.navigation.SubGuardNavGraph
import com.davinder.subguard.ui.theme.SubGuardTheme
import com.davinder.subguard.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        com.davinder.subguard.data.AppearancePreferences.init(this)

        val deepLinkSubscriptionId = intent.getStringExtra("subscriptionId")

        setContent {
            val preferences = remember { PreferencesManager(applicationContext) }
            val themeMode by preferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            SubGuardTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    SubGuardNavGraph(
                        navController = navController,
                        deepLinkSubscriptionId = deepLinkSubscriptionId,
                        onSignOut = {
                            // Safely navigate to SignIn / Welcome screen while popping ALL backstack
                            // Git change detection tes
                            navController.navigate(Screen.SignIn.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}