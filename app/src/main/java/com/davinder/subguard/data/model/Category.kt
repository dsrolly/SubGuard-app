package com.davinder.subguard.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector

data class SubscriptionCategory(val label: String, val icon: ImageVector)

/** Fixed category list with icons — no emoji, as decided. */
object Categories {
    val all = listOf(
        SubscriptionCategory("Entertainment", Icons.Outlined.Movie),
        SubscriptionCategory("Fitness", Icons.Outlined.FitnessCenter),
        SubscriptionCategory("Cloud Storage", Icons.Outlined.CloudQueue),
        SubscriptionCategory("Utilities", Icons.Outlined.Bolt),
        SubscriptionCategory("Education", Icons.Outlined.School),
        SubscriptionCategory("Gaming", Icons.Outlined.SportsEsports),
        SubscriptionCategory("Music", Icons.Outlined.MusicNote),
        SubscriptionCategory("News & Reading", Icons.Outlined.Article),
        SubscriptionCategory("Productivity", Icons.Outlined.WorkOutline),
        SubscriptionCategory("Other", Icons.Outlined.Category)
    )
}