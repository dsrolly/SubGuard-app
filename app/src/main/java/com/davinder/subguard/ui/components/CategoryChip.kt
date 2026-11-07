package com.davinder.subguard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.davinder.subguard.data.model.SubscriptionCategory
import com.davinder.subguard.ui.theme.SubGuardAccentGradient

/**
 * Reusable category chip. Selected = gradient accent background (from
 * ui/theme/GradientAccents.kt). Unselected = neutral surface, theme-aware.
 */
@Composable
fun CategoryChip(
    category: SubscriptionCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val backgroundModifier = if (selected) {
        Modifier.clip(shape).background(SubGuardAccentGradient)
    } else {
        Modifier.clip(shape).background(MaterialTheme.colorScheme.surfaceVariant)
    }
    val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = backgroundModifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = category.label,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = category.label,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall
        )
    }
}