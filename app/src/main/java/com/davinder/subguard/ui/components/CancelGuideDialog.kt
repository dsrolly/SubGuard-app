package com.davinder.subguard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davinder.subguard.ui.theme.SubGuardAccentGradient

@Composable
fun CancelGuideDialog(
    serviceName: String,
    onDismiss: () -> Unit,
    onShowSteps: () -> Unit,
    onDeleteFromApp: () -> Unit
) {
    SubGuardDialog(
        onDismiss = onDismiss,
        icon = Icons.Outlined.OpenInNew,
        title = "Cancel $serviceName",
        message = "SubGuard tracks your plan, but $serviceName can only be cancelled on its own app or website.",
        primaryText = "Show cancel steps",
        onPrimary = onShowSteps,
        secondaryText = "Already cancelled? Remove it",
        onSecondary = onDeleteFromApp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            GuideStep(1, "Tap \"Show cancel steps\" - Google opens the latest official guide for $serviceName.")
            GuideStep(2, "Cancel in $serviceName's account settings (or in Play Store / App Store subscriptions if you bought it there).")
            GuideStep(3, "Come back and remove it here so you stop getting renewal alerts.")
        }
    }
}

@Composable
private fun GuideStep(number: Int, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(22.dp).clip(CircleShape).background(SubGuardAccentGradient),
            contentAlignment = Alignment.Center
        ) {
            Text("$number", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Box(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
