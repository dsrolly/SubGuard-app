package com.davinder.subguard.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.davinder.subguard.ui.theme.SubGuardAccentGradient
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.davinder.subguard.ui.theme.AzureButtonContainer
import com.davinder.subguard.ui.theme.OnAzureButtonContainer

private val BrandIndigo = Color(0xFF6366F1)
private val BrandTeal = Color(0xFF14B8A6)
private val DangerRed = Color(0xFFDC2626)

@Composable
private fun isDarkSurface(): Boolean = MaterialTheme.colorScheme.onBackground.luminance() > 0.5f

/** Accent glow colour; falls back to brand indigo when the chosen accent is near black/white. */
@Composable
private fun glowAccent(): Color {
    val p = MaterialTheme.colorScheme.primary
    val l = p.luminance()
    return if (l < 0.12f || l > 0.85f) BrandIndigo else p
}

/**
 * Full-screen ambient gradient (base colour + two soft glows). Put it ONCE at the root
 * (MainActivity). All screens have transparent backgrounds, so this shows through.
 */
@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = isDarkSurface()
    val accent = glowAccent()
    val base = if (dark) Color(0xFF070D1F) else Color(0xFFEEF3FA)
    val glowA = accent.copy(alpha = if (dark) 0.30f else 0.20f)
    val glowB = BrandTeal.copy(alpha = if (dark) 0.20f else 0.16f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(base)
            .drawBehind {
                val r = size.maxDimension * 0.75f
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(glowA, Color.Transparent),
                        center = Offset(size.width * 0.95f, size.height * 0.04f),
                        radius = r
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(glowB, Color.Transparent),
                        center = Offset(size.width * 0.02f, size.height * 0.96f),
                        radius = r
                    )
                )
            },
        content = content
    )
}

/** Gradient hairline used on every glass edge (bright top-left, fading bottom-right). */
@Composable
fun glassBorderBrush(): Brush {
    val dark = isDarkSurface()
    return Brush.linearGradient(
        colors = if (dark) listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.06f))
        else listOf(Color.White, Color(0xFFCBD5E1).copy(alpha = 0.55f))
    )
}

/** Glass card: translucent gradient fill + hairline border + soft shadow. */
@Composable
fun Modifier.glass(
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 6.dp
): Modifier {
    val dark = isDarkSurface()
    val glow = glowAccent().copy(alpha = if (dark) 0.14f else 0.07f)
    val fill = if (dark) Brush.verticalGradient(
        listOf(Color(0xFF1E2A47), Color(0xFF172037))
    ) else Brush.verticalGradient(
        listOf(Color(0xFFFFFFFF), Color(0xFFF4F8FC))
    )
    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = if (dark) 0.35f else 0.08f),
            spotColor = glowAccent().copy(alpha = if (dark) 0.25f else 0.18f)
        )
        .clip(shape)
        .background(fill)
        .drawBehind {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(glow, Color.Transparent),
                    center = Offset(size.width, 0f),
                    radius = size.maxDimension * 0.8f
                )
            )
        }
        .border(1.dp, glassBorderBrush(), shape)
}

/**
 * ONE dialog for the whole app. Opaque card (readable in light + dark), gradient icon circle,
 * safe action = filled primary button, other action = outlined button (red if destructive).
 */
@Composable
fun SubGuardDialog(
    onDismiss: () -> Unit,
    icon: ImageVector,
    title: String,
    message: String? = null,
    primaryText: String,
    onPrimary: () -> Unit,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null,
    destructive: Boolean = false,
    primaryEnabled: Boolean = true,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    val accent = glowAccent()
    val iconBrush = if (destructive)
        Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C)))
    else Brush.linearGradient(listOf(accent, BrandTeal))

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, glassBorderBrush()),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(iconBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                if (message != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                if (content != null) {
                    Spacer(Modifier.height(16.dp))
                    content()
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onPrimary,
                    enabled = primaryEnabled,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzureButtonContainer,
                        contentColor = OnAzureButtonContainer
                    )
                ) {
                    Text(primaryText, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                if (secondaryText != null && onSecondary != null) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onSecondary,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = RoundedCornerShape(26.dp),
                        border = BorderStroke(
                            1.dp,
                            if (destructive) DangerRed.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (destructive) DangerRed else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(secondaryText, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}


/**
 * Drop-in replacement for Material Surface(...) used for every card in the app.
 * Same parameter names, so existing call sites only change their name.
 */
@Composable
fun PremiumSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var m = modifier.glass(shape, shadowElevation + 4.dp)
    if (onClick != null) m = m.clickable(enabled = enabled, onClick = onClick)
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(modifier = m, propagateMinConstraints = true) { content() }
    }
}

/** Primary call-to-action button: accent gradient, glow shadow, white label. */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val accent = AzureButtonContainer
    val shape = RoundedCornerShape(28.dp)
    val brush = Brush.linearGradient(listOf(accent, androidx.compose.ui.graphics.lerp(accent, BrandTeal, 0.35f)))
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = if (enabled) 10.dp else 0.dp,
                shape = shape,
                ambientColor = Color.Transparent,
                spotColor = accent.copy(alpha = 0.55f)
            )
            .clip(shape)
            .background(if (enabled) brush else Brush.linearGradient(listOf(Color(0xFF94A3B8), Color(0xFF94A3B8))))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = OnAzureButtonContainer,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}


/**
 * Drop-in replacement for Material Button(...) for every primary button in the app.
 * Same parameter names (colors/elevation are accepted but ignored) so call sites only change the name.
 * Look: theme-aware dark->accent gradient, glow shadow, glossy top edge, white label.
 */
@Composable
fun PremiumButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    colors: ButtonColors? = null,
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
    content: @Composable RowScope.() -> Unit
) {
    val glowColor = com.davinder.subguard.ui.theme.AzureButtonContainer
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 58.dp, minHeight = 40.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = shape,
                ambientColor = Color.Transparent,
                spotColor = glowColor.copy(alpha = 0.55f)
            )
            .clip(shape)
            .background(SubGuardAccentGradient)
            .border(
                1.dp,
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.30f), Color.Transparent)),
                shape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { role = Role.Button }
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }
    }
}

/** Selectable pill: selected = theme gradient + white text, unselected = soft neutral. */
@Composable
fun PremiumChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (selected) Modifier.background(SubGuardAccentGradient)
                else Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
    }
}
