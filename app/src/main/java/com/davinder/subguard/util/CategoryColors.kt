package com.davinder.subguard.util

import androidx.compose.ui.graphics.Color

/** Maps each category to an accent color used as a card's left border in lists. */
fun categoryAccentColor(category: String): Color = when (category) {
    "Entertainment" -> Color(0xFFEC4899)
    "Fitness" -> Color(0xFFF59E0B)
    "Cloud Storage" -> Color(0xFF38BDF8)
    "Utilities" -> Color(0xFFFACC15)
    "Education" -> Color(0xFF8B5CF6)
    "Gaming" -> Color(0xFF22C55E)
    "Music" -> Color(0xFF10B981)
    "News & Reading" -> Color(0xFF6366F1)
    "Productivity" -> Color(0xFFEF4444)
    else -> Color(0xFF9CA3AF)
}