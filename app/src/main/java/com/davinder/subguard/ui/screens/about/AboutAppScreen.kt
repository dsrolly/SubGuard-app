package com.davinder.subguard.ui.screens.about

import com.davinder.subguard.ui.components.PremiumButton
import com.davinder.subguard.ui.components.PremiumSurface
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.davinder.subguard.ui.theme.AzureButtonContainer
import com.davinder.subguard.ui.theme.AzureHeroGradient
import com.davinder.subguard.ui.theme.AzurePrimary
import com.davinder.subguard.ui.theme.OnAzureButtonContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var activeLegalDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Shield Logo (icon color auto-adjusts to the gradient behind it)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AzureHeroGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = OnAzureButtonContainer,
                    modifier = Modifier.size(42.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SubGuard", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text("v1.0.0 • Build 73", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Support Rows
            PremiumSurface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    AboutRow(icon = Icons.Outlined.Mail, title = "Send feedback") {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@subguard.com")
                            putExtra(Intent.EXTRA_SUBJECT, "SubGuard App Feedback")
                        }
                        context.startActivity(Intent.createChooser(emailIntent, "Send Feedback"))
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    AboutRow(icon = Icons.Outlined.StarRate, title = "Rate SubGuard") {
                        val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                        runCatching { context.startActivity(playStoreIntent) }.onFailure {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")))
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    AboutRow(icon = Icons.Outlined.Share, title = "Share SubGuard") {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "SubGuard - Subscription Manager")
                            putExtra(Intent.EXTRA_TEXT, "Manage recurring expenses with SubGuard! Download today: ${com.davinder.subguard.util.AppLinks.DOWNLOAD}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share SubGuard"))
                    }
                }
            }

            // Legal Rows
            PremiumSurface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    AboutRow(icon = Icons.Outlined.Info, title = "Help centre") {
                        com.davinder.subguard.util.AppLinks.open(context, com.davinder.subguard.util.AppLinks.HELP)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    AboutRow(icon = Icons.Outlined.Article, title = "Blog & guides") {
                        com.davinder.subguard.util.AppLinks.open(context, com.davinder.subguard.util.AppLinks.BLOG)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    AboutRow(icon = Icons.Outlined.Info, title = "Account deletion guide") {
                        com.davinder.subguard.util.AppLinks.open(context, com.davinder.subguard.util.AppLinks.DELETE_ACCOUNT)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    AboutRow(icon = Icons.Outlined.Policy, title = "Privacy Policy") {
                        com.davinder.subguard.util.AppLinks.open(context, com.davinder.subguard.util.AppLinks.PRIVACY)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    AboutRow(icon = Icons.Outlined.Gavel, title = "Terms of Service") {
                        com.davinder.subguard.util.AppLinks.open(context, com.davinder.subguard.util.AppLinks.TERMS)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    AboutRow(icon = Icons.Outlined.Code, title = "Open-source licenses") {
                        com.davinder.subguard.util.AppLinks.open(context, com.davinder.subguard.util.AppLinks.LICENSES)
                    }
                }
            }
        }
    }

    // Modern Legal Information Dialog
    if (activeLegalDialog != null) {
        val (title, content) = activeLegalDialog!!
        Dialog(
            onDismissRequest = { activeLegalDialog = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(22.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        // FIX: slate container + auto-readable text (was AzurePrimary + Color.White)
                        PremiumButton(
                            onClick = { activeLegalDialog = null },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AzureButtonContainer,
                                contentColor = OnAzureButtonContainer
                            )
                        ) {
                            Text("Close", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = AzurePrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}