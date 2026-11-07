package com.davinder.subguard.ui.screens.settings

import com.davinder.subguard.ui.components.PremiumButton
import com.davinder.subguard.ui.components.PremiumSurface
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davinder.subguard.data.AppearancePreferences
import com.davinder.subguard.ui.components.LogoIndigo
import com.davinder.subguard.ui.components.LogoTeal
import com.davinder.subguard.ui.theme.AzureButtonContainer
import com.davinder.subguard.ui.theme.AzurePrimary
import com.davinder.subguard.ui.theme.OnAzureButtonContainer
import com.davinder.subguard.viewmodel.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.davinder.subguard.ui.components.performTick

@Composable
fun SettingsScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCurrencies: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToDashboardDisplay: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onNavigateToTags: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToWhatsNew: () -> Unit = {},
    onNavigateToDeleteAccount: () -> Unit = {},
    onSignIn: () -> Unit = {},
    onSignedOut: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val resetState by viewModel.passwordReset.collectAsState()
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser

    var showResetDialog by remember { mutableStateOf(false) }

    // Password reset ka result (success / error) Toast mein dikhao, phir clear karo.
    LaunchedEffect(resetState) {
        val text = resetState.message ?: resetState.error
        if (text != null) {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
            viewModel.clearPasswordResetResult()
        }
    }

    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    val initials = remember(user) {
        val name = user?.displayName.orEmpty()
        val email = user?.email.orEmpty()
        when {
            name.isNotBlank() -> name.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .map { it.first().uppercase() }
                .joinToString("")
            email.isNotBlank() -> email.take(2).uppercase()
            else -> "U"
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Column {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Preferences, account sync & data options",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 1. Interactive Profile Summary Card (Clicking navigates to Full Profile Details)
            PremiumSurface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (user != null) onNavigateToProfile() else onSignIn()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar with Initials
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(AzurePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user != null && initials.isNotBlank()) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AzurePrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = AzurePrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (user != null) (user.displayName ?: "SubGuard User") else "Sign in to SubGuard",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (user != null) (user.email ?: "Account Details") else "Back up & restore your subscriptions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Profile",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2. PREFERENCES GROUP
            SettingsSectionTitle(title = "PREFERENCES")
            PremiumSurface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingNavRow(
                        icon = Icons.Outlined.CurrencyExchange,
                        title = "Main currency",
                        badgeText = uiState.mainCurrency,
                        onClick = onNavigateToCurrencies
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    SettingNavRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Notification settings",
                        onClick = onNavigateToNotifications
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    SettingNavRow(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "Dashboard display",
                        onClick = onNavigateToDashboardDisplay
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    SettingNavRow(
                        icon = Icons.Outlined.DarkMode,
                        title = "Appearance & Themes",
                        badgeText = uiState.themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                        onClick = onNavigateToAppearance
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    // Haptic Feedback Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Vibration,
                                contentDescription = null,
                                tint = AzurePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Haptic feedback",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Gentle taps on key actions",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = uiState.hapticEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.onHapticChange(enabled)
                                if (enabled) performTick(context)   // ON karte hi ek tick milega
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OnAzureButtonContainer,
                                checkedTrackColor = AzureButtonContainer,
                                checkedBorderColor = AzureButtonContainer,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                uncheckedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }

            // 3. DATA & BACKUP GROUP
            SettingsSectionTitle(title = "DATA")
            PremiumSurface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingNavRow(
                        icon = Icons.Outlined.CloudUpload,
                        title = "Backup & export",
                        onClick = onNavigateToBackup
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    SettingNavRow(
                        icon = Icons.Outlined.Label,
                        title = "Tags & custom categories",
                        onClick = onNavigateToTags
                    )
                }
            }

            // 4. SUPPORT & ABOUT GROUP
            SettingsSectionTitle(title = "SUPPORT")
            PremiumSurface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingNavRow(
                        icon = Icons.Outlined.Info,
                        title = "Help & about SubGuard",
                        onClick = onNavigateToAbout
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    SettingNavRow(
                        icon = Icons.Outlined.AutoAwesome,
                        title = "What's new",
                        onClick = onNavigateToWhatsNew
                    )
                }
            }

            // 5. ACCOUNT GROUP (sirf signed-in users ko dikhta hai)
            if (user != null) {
                SettingsSectionTitle(title = "ACCOUNT")
                PremiumSurface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Change password: sirf email/password users ke liye
                        if (uiState.canChangePassword) {
                            SettingNavRow(
                                icon = Icons.Outlined.LockReset,
                                title = "Change password",
                                onClick = { showResetDialog = true }
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        }

                        SettingNavRow(
                            icon = Icons.Outlined.DeleteForever,
                            title = "Delete account",
                            isDestructive = true,
                            onClick = onNavigateToDeleteAccount
                        )
                    }
                }
            }

            // 6. SIGN IN (only for guests). Sign out Profile mein hai.
            if (user == null) {
                PremiumButton(
                    onClick = onSignIn,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzureButtonContainer,
                        contentColor = OnAzureButtonContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Sign in with Google", fontWeight = FontWeight.Bold)
                }
            }

            // Footer Version
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SUBGUARD • V$versionName",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Floating bottom bar overlaps the content, so leave room: the footer text
            // and the last card can scroll fully above the bar + "+" button.
            Spacer(
                modifier = Modifier
                    .navigationBarsPadding()
                    .height(90.dp)
            )
        }
    }

    if (showResetDialog) {
        PasswordResetDialog(
            email = user?.email.orEmpty(),
            onSend = {
                showResetDialog = false
                viewModel.sendPasswordReset()
            },
            onDismiss = { showResetDialog = false }
        )
    }
}

@Composable
private fun PasswordResetDialog(
    email: String,
    onSend: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            shadowElevation = 18.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(LogoIndigo, LogoTeal))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LockReset,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Change password?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (email.isNotBlank()) {
                        "We will send a password reset link to $email. Open it to set a new password."
                    } else {
                        "We will send a password reset link to your account email."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))
                PremiumButton(
                    onClick = onSend,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzurePrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Send reset link", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    val isDark = AppearancePreferences.isDarkModeState.value
    val rawAccent = AppearancePreferences.currentAccentState.value
    val titleColor = if (isDark && rawAccent.luminance() < 0.12f) {
        Color(0xFF94A3B8)
    } else {
        AzurePrimary
    }

    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp
        ),
        color = titleColor,
        modifier = Modifier.padding(start = 8.dp)
    )
}

@Composable
private fun SettingNavRow(
    icon: ImageVector,
    title: String,
    badgeText: String? = null,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val isDark = AppearancePreferences.isDarkModeState.value
    val rawAccent = AppearancePreferences.currentAccentState.value
    val normalIconColor = if (isDark && rawAccent.luminance() < 0.12f) {
        Color(0xFFF8FAFC)
    } else {
        AzurePrimary
    }
    val errorColor = MaterialTheme.colorScheme.error
    val iconColor = if (isDestructive) errorColor else normalIconColor
    val titleColor = if (isDestructive) errorColor else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = titleColor
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = iconColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (isDestructive) errorColor.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}