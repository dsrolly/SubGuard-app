package com.davinder.subguard.ui.screens.subscriptiondetail

import com.davinder.subguard.ui.components.PremiumButton
import com.davinder.subguard.ui.components.PremiumSurface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davinder.subguard.data.model.SubscriptionStatus
import com.davinder.subguard.ui.theme.AzureHeroGradient
import com.davinder.subguard.ui.theme.AzurePrimary
import com.davinder.subguard.viewmodel.SubscriptionDetailViewModel

@Composable
fun SubscriptionDetailScreen(
    subscriptionId: String,
    onBack: () -> Unit,
    onEditClick: (String) -> Unit,
    viewModel: SubscriptionDetailViewModel = viewModel()
) {
    val sub by viewModel.subscription.collectAsState()
    val isDeleted by viewModel.isDeleted.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showCancelGuide by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(subscriptionId) {
        viewModel.load(subscriptionId)
    }

    LaunchedEffect(isDeleted) {
        if (isDeleted) onBack()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (sub == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AzurePrimary)
            }
            return@Surface
        }

        val currentSub = sub!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Subscription Detail",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero Details Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(AzureHeroGradient)
                    .padding(22.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentSub.category.uppercase(),
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currentSub.status.name,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentSub.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${currentSub.currency} ${"%.2f".format(currentSub.amount)} / ${currentSub.billingCycle.name.lowercase()}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Preferences Card (Reminders & Pause Switches with Azure Colors)
            PremiumSurface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "PREFERENCES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        ),
                        color = AzurePrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reminders Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Renewal Reminders", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Receive notifications 3, 1, and 0 days prior",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = currentSub.remindersEnabled,
                            onCheckedChange = viewModel::onToggleReminders,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AzurePrimary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )

                    // Pause Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pause Subscription", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Temporarily hide from burn-rate & silence alerts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = currentSub.status == SubscriptionStatus.PAUSED,
                            onCheckedChange = viewModel::onTogglePaused,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AzurePrimary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 1. Edit Button (Cyan High-Contrast)
                PremiumButton(
                    onClick = { onEditClick(currentSub.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzurePrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Subscription Details", fontWeight = FontWeight.Bold, color = Color.White)
                }

                // 2. Cancel guide - ONLINE services only (offline: gym, rent... have no guide)
                if (com.davinder.subguard.util.CancelGuide.isOnlineService(currentSub.name, currentSub.category)) {
                    OutlinedButton(
                        onClick = { showCancelGuide = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                        ),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel this subscription", fontWeight = FontWeight.SemiBold)
                    }
                }

                // 3. Delete Subscription (Red Accent Warning Card)
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete subscription permanently", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showCancelGuide) {
        val subName = sub?.name.orEmpty()
        com.davinder.subguard.ui.components.CancelGuideDialog(
            serviceName = subName,
            onDismiss = { showCancelGuide = false },
            onShowSteps = {
                showCancelGuide = false
                val url = com.davinder.subguard.util.CancelGuide.cancelSearchUrl(subName)
                runCatching {
                    context.startActivity(
                        android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                    )
                }
            },
            onDeleteFromApp = {
                showCancelGuide = false
                showDeleteConfirm = true
            }
        )
    }

    if (showDeleteConfirm) {
        com.davinder.subguard.ui.components.SubGuardDialog(
            onDismiss = { showDeleteConfirm = false },
            icon = Icons.Outlined.Delete,
            title = "Delete subscription?",
            message = "This permanently removes it and its billing history. This cannot be undone.",
            primaryText = "Keep it",
            onPrimary = { showDeleteConfirm = false },
            secondaryText = "Yes, delete",
            onSecondary = {
                showDeleteConfirm = false
                viewModel.delete()
            },
            destructive = true
        )
    }
}
