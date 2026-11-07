package com.davinder.subguard.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Start and End accent colors for the Cool Azure / Arctic theme
val GradientStart = Color(0xFF0F2B48) // Deep Arctic Navy
val GradientEnd = Color(0xFF0284C7)   // Vibrant Azure Cyan

// The primary accent gradient used across the app (hero cards, CTA buttons, docked center add button)
val SubGuardAccentGradient: Brush
    get() = AccentGradient