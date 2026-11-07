package com.davinder.subguard.ui.screens.dashboard

import com.davinder.subguard.ui.components.PremiumSurface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import com.davinder.subguard.ui.components.glass
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davinder.subguard.data.AppearancePreferences
import com.davinder.subguard.ui.theme.AzurePrimary
import com.davinder.subguard.ui.theme.readableContent
import com.davinder.subguard.util.categoryAccentColor
import com.davinder.subguard.viewmodel.DashboardUiState
import com.davinder.subguard.viewmodel.DashboardViewModel
import com.davinder.subguard.viewmodel.UpcomingRenewal

private val AzureSubtleLines = Color(0xFF38BDF8).copy(alpha = 0.22f)

@Composable
fun DashboardScreen(
    onViewAllClick: () -> Unit,
    onSubscriptionClick: (String) -> Unit,
    onConverterClick: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Persistent reactive values linked to AppearancePreferences
    val cardOpacity = AppearancePreferences.widgetOpacityState.floatValue
    val showSpend = AppearancePreferences.widgetSpendState.value
    val showAlerts = AppearancePreferences.widgetAlertsState.value

    // Raw accent: only for the hero card gradient/background
    val currentAccent = AppearancePreferences.currentAccentState.value
    // UI accent: icons, text, buttons (becomes white in dark + black theme)
    val uiAccent = AzurePrimary

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title & Converter Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Track and manage cash commitments",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onConverterClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Currency converter",
                            tint = uiAccent
                        )
                    }
                }
            }

            // Spending Hero Card with opacity & hide-spend toggles
            item {
                ModernConstellationHeroCard(
                    uiState = uiState,
                    accentColor = currentAccent,
                    cardOpacity = cardOpacity,
                    showSpend = showSpend
                )
            }

            // Bento Stat Cards
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BentoStatCard(
                        icon = Icons.Outlined.CheckCircleOutline,
                        iconTint = Color(0xFF10B981),
                        label = "Active Plans",
                        value = "${uiState.activeCount}",
                        caption = "Subscribed",
                        modifier = Modifier.weight(1f)
                    )
                    BentoStatCard(
                        icon = Icons.Outlined.NotificationsActive,
                        iconTint = Color(0xFFF59E0B),
                        label = "Due Soon",
                        value = "${uiState.dueSoonCount}",
                        caption = "Within 7 days",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Upcoming Renewals Header & List (controlled by showAlerts)
            if (showAlerts) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upcoming Renewals",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        TextButton(onClick = onViewAllClick) {
                            Text(
                                text = "View all",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = uiAccent
                            )
                        }
                    }
                }

                if (uiState.upcoming.isEmpty()) {
                    item {
                        PremiumSurface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(26.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "All clear for the next 30 days",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "No renewals scheduled in this cycle.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.upcoming, key = { it.subscription.id }) { upcoming ->
                        ModernUpcomingRow(
                            upcoming = upcoming,
                            onClick = { onSubscriptionClick(upcoming.subscription.id) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(86.dp)) }
        }
    }
}

@Composable
private fun ModernConstellationHeroCard(
    uiState: DashboardUiState,
    accentColor: Color,
    cardOpacity: Float,
    showSpend: Boolean
) {
    val currency = uiState.currency
    val cashFlow = uiState.cashFlowView
    val viewLabel = uiState.spendingView.label.uppercase()

    // Cash-flow ON  -> real bills of the period. OFF -> estimated average.
    val headline = if (cashFlow) "CASH FLOW · $viewLabel" else "ESTIMATED $viewLabel SPEND"
    val bigAmount = if (cashFlow) uiState.periodTotal else uiState.estimatedAmount
    val chipText = if (cashFlow) "$currency · actual" else "$currency / ${uiState.spendingView.unit}"

    val isBlackTheme = accentColor.luminance() < 0.15f

    val onHero = accentColor.readableContent()
    val heroIsDarkCard = onHero == Color.White

    // Scale child elements opacity with cardOpacity
    val innerPillColor = if (heroIsDarkCard) {
        Color.Black.copy(alpha = 0.35f * cardOpacity)
    } else {
        Color.White.copy(alpha = 0.35f * cardOpacity)
    }
    val chipColor = onHero.copy(alpha = 0.18f * cardOpacity)

    val dynamicGradient = if (isBlackTheme) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF020617).copy(alpha = cardOpacity),
                Color(0xFF1E293B).copy(alpha = cardOpacity)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                accentColor.copy(alpha = cardOpacity),
                accentColor.copy(alpha = cardOpacity * 0.75f)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            // Fade shadow away as cardOpacity drops to prevent dark rectangle artifacts
            .shadow(
                elevation = (16.dp * cardOpacity.coerceIn(0f, 1f)),
                shape = RoundedCornerShape(26.dp),
                spotColor = if (isBlackTheme) Color.Black.copy(alpha = cardOpacity) else accentColor.copy(alpha = 0.35f * cardOpacity),
                ambientColor = Color.Transparent
            )
            .clip(RoundedCornerShape(26.dp))
            .background(dynamicGradient)
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.50f * cardOpacity), Color.White.copy(alpha = 0.06f * cardOpacity))
                ),
                RoundedCornerShape(26.dp)
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            // glow orb (top-right) + soft orb (bottom-left) + diagonal sheen
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.26f * cardOpacity), Color.Transparent),
                    center = Offset(w * 0.90f, h * 0.02f),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(w * 0.90f, h * 0.02f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF14B8A6).copy(alpha = 0.28f * cardOpacity), Color.Transparent),
                    center = Offset(w * 0.05f, h * 1.0f),
                    radius = w * 0.5f
                ),
                radius = w * 0.5f,
                center = Offset(w * 0.05f, h * 1.0f)
            )
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.10f * cardOpacity), Color.Transparent),
                    start = Offset(0f, h * 0.1f),
                    end = Offset(w * 0.75f, h)
                )
            )
            val lineAlpha = (0.12f * cardOpacity)
            val lineColor = when {
                isBlackTheme -> Color.White.copy(alpha = lineAlpha)
                heroIsDarkCard -> AzureSubtleLines.copy(alpha = (0.22f * cardOpacity))
                else -> onHero.copy(alpha = lineAlpha)
            }
            drawLine(
                color = lineColor,
                start = Offset(w * 0.70f, 0f),
                end = Offset(w, h * 0.85f),
                strokeWidth = 1.5f
            )
            drawLine(
                color = lineColor,
                start = Offset(w * 0.82f, 0f),
                end = Offset(w * 0.65f, h),
                strokeWidth = 1.5f
            )
            drawLine(
                color = lineColor,
                start = Offset(w * 0.60f, h * 0.35f),
                end = Offset(w, h * 0.40f),
                strokeWidth = 1.2f
            )
            drawLine(
                color = lineColor,
                start = Offset(w * 0.72f, h),
                end = Offset(w, h * 0.15f),
                strokeWidth = 1.5f
            )
        }

        Column(modifier = Modifier.padding(22.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = headline,
                    color = onHero.copy(alpha = 0.90f),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = chipColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TrendingUp,
                            contentDescription = null,
                            tint = onHero,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = chipText,
                            color = onHero,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (showSpend) "$currency ${"%.2f".format(bigAmount)}" else "••••••••",
                color = onHero,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.8).sp
                )
            )

            if (cashFlow && uiState.periodLabel.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = uiState.periodLabel,
                    color = onHero.copy(alpha = 0.80f),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            if (uiState.conversionIncomplete && showSpend) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "*Some currencies could not be converted offline",
                    color = onHero.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Cash-flow: spent vs left for the current period
            if (cashFlow) {
                val progress = if (uiState.periodTotal > 0.0) {
                    (uiState.periodSpent / uiState.periodTotal).toFloat().coerceIn(0f, 1f)
                } else 0f

                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = onHero,
                    trackColor = onHero.copy(alpha = 0.25f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (showSpend) "$currency ${"%.0f".format(uiState.periodSpent)} spent" else "•••• spent",
                        color = onHero,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (showSpend) "$currency ${"%.0f".format(uiState.periodLeft)} left" else "•••• left",
                        color = onHero,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = innerPillColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Annual Run Rate",
                        color = onHero.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (showSpend) "$currency ${"%.0f".format(uiState.monthlyBurnRate * 12)}" else "••••••••",
                        color = onHero,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun BentoStatCard(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .glass(shape, 8.dp)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(iconTint.copy(alpha = 0.20f), Color.Transparent),
                        center = Offset(size.width, 0f),
                        radius = size.width * 0.95f
                    )
                )
            }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(iconTint.copy(alpha = 0.30f), iconTint.copy(alpha = 0.10f))
                        )
                    )
                    .border(1.dp, iconTint.copy(alpha = 0.28f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModernUpcomingRow(upcoming: UpcomingRenewal, onClick: () -> Unit) {
    val categoryColor = categoryAccentColor(upcoming.subscription.category)
    val shape = RoundedCornerShape(20.dp)
    val isUrgent = upcoming.daysAway <= 3
    val urgentRed = Color(0xFFEF4444)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass(shape, 4.dp)
            .clickable(onClick = onClick)
            .drawBehind {
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(categoryColor.copy(alpha = 0.16f), Color.Transparent),
                        endX = size.width * 0.55f
                    )
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(categoryColor, categoryColor.copy(alpha = 0.65f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = upcoming.subscription.name.take(1).uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = upcoming.subscription.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "${upcoming.subscription.currency} ${upcoming.subscription.amount} • " +
                            upcoming.subscription.billingCycle.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(
                    if (isUrgent) urgentRed.copy(alpha = 0.14f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                )
                .border(
                    1.dp,
                    if (isUrgent) urgentRed.copy(alpha = 0.35f) else Color.Transparent,
                    RoundedCornerShape(50)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (upcoming.daysAway == 0) "Today" else "${upcoming.daysAway}d left",
                color = if (isUrgent) urgentRed else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
