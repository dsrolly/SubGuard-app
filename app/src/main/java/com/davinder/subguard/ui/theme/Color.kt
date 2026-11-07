package com.davinder.subguard.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.davinder.subguard.data.AppearancePreferences

// --- Core Brand Tokens ---
val ArcticNavy = Color(0xFF0F2B48)
val AzureCyan = Color(0xFF0284C7)
val AzureSky = Color(0xFF38BDF8)

// --- Helpers ---

/**
 * Dark mode + black theme me accent white ban jata hai (taaki dark background par dikhe).
 * Baaki sab cases me chosen accent hi rehta hai.
 */
fun resolveAccent(raw: Color, isDark: Boolean): Color =
    if (isDark && raw.luminance() < 0.15f) Color(0xFFF8FAFC) else raw

/**
 * Kisi bhi background color par readable text/icon color.
 * Light background -> dark content, dark background -> white content.
 */
fun Color.readableContent(): Color =
    if (this.luminance() > 0.5f) Color(0xFF0B132B) else Color.White

// Master Unified Accent:
// Dark Mode + Black Theme me icons aur section titles crisp white (0xFFF8FAFC).
// Light Mode + Black Theme me pure dark. Baaki themes me chosen color.
val AzurePrimary: Color
    get() {
        val raw = AppearancePreferences.currentAccentState.value
        val isDark = AppearancePreferences.isDarkModeState.value
        return resolveAccent(raw, isDark)
    }

// Unified Button Background: Dark Mode + Black Theme me slate container
val AzureButtonContainer: Color
    get() {
        val raw = AppearancePreferences.currentAccentState.value
        val isDark = AppearancePreferences.isDarkModeState.value
        return if (isDark && raw.luminance() < 0.15f) {
            Color(0xFF334155)
        } else {
            raw
        }
    }

// Content colors: hamesha background ke hisab se auto-calculate hote hain
val OnAzurePrimary: Color
    get() = AzurePrimary.readableContent()

val OnAzureButtonContainer: Color
    get() = AzureButtonContainer.readableContent()

// Theme-aware accent gradient used by EVERY button, selected chip, FAB and hero.
// Follows the accent chosen in Appearance & Themes (dark shade -> accent, left to right).
val AccentGradient: Brush
    get() {
        val c = AzureButtonContainer
        val nearBlack = c.luminance() < 0.10f
        val start = if (nearBlack) Color(0xFF0B132B)
        else androidx.compose.ui.graphics.lerp(c, Color.Black, 0.40f)
        val end = if (nearBlack) Color(0xFF334155) else c
        return Brush.linearGradient(listOf(start, end))
    }

val AzureHeroGradient: Brush
    get() = AccentGradient

// --- Light Theme Tokens ---
val LightBackground = Color(0xFFF1F6FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE2E8F0)
val LightOnBackground = Color(0xFF0F172A)
val LightOnSurface = Color(0xFF0F172A)
val LightOnSurfaceVariant = Color(0xFF475569)
val LightOutline = Color(0xFFCBD5E1)
val LightErrorContainer = Color(0xFFFEE2E2)
val LightOnErrorContainer = Color(0xFFDC2626)

// --- Dark Theme Tokens ---
val DarkBackground = Color(0xFF0B132B)        // Deep Midnight
val DarkSurface = Color(0xFF192237)           // Elevated Cards
val DarkSurfaceVariant = Color(0xFF28354E)    // Containers & Inputs
val DarkOnBackground = Color(0xFFFFFFFF)      // Pure White Text
val DarkOnSurface = Color(0xFFF8FAFC)         // Crisp Card Headings
val DarkOnSurfaceVariant = Color(0xFFCBD5E1)  // Bright Visible Subtitles & Icons
val DarkOutline = Color(0xFF3B4D6D)           // Crisp Borders
val DarkErrorContainer = Color(0xFF7F1D1D)
val DarkOnErrorContainer = Color(0xFFFECACA)