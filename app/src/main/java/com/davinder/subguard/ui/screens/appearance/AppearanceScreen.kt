package com.davinder.subguard.ui.screens.appearance

import com.davinder.subguard.ui.components.PremiumButton
import com.davinder.subguard.ui.components.PremiumSurface
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davinder.subguard.data.AppearancePreferences
import com.davinder.subguard.ui.theme.AzureButtonContainer
import com.davinder.subguard.ui.theme.AzurePrimary
import com.davinder.subguard.ui.theme.ThemeMode
import com.davinder.subguard.viewmodel.SettingsViewModel

data class ThemeColorItem(
    val id: String,
    val name: String,
    val color: Color
)

val PRESET_THEME_COLORS = listOf(
    ThemeColorItem("azure", "Azure Cyan", Color(0xFF0284C7)),
    ThemeColorItem("rose", "Rose Pink", Color(0xFFF43F5E)),
    ThemeColorItem("amber", "Amber Gold", Color(0xFFF59E0B)),
    ThemeColorItem("emerald", "Emerald Green", Color(0xFF10B981)),
    ThemeColorItem("violet", "Royal Violet", Color(0xFF8B5CF6)),
    ThemeColorItem("teal", "Ocean Teal", Color(0xFF14B8A6)),
    ThemeColorItem("indigo", "Deep Indigo", Color(0xFF6366F1)),
    ThemeColorItem("coral", "Coral Orange", Color(0xFFFF6B6B)),
    ThemeColorItem("black", "OLED Black", Color(0xFF090D16))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedColorId by remember { mutableStateOf(AppearancePreferences.getSavedColorId()) }
    var showWidgetSheet by remember { mutableStateOf(false) }

    var widgetOpacity by remember { mutableFloatStateOf(AppearancePreferences.widgetOpacityState.floatValue) }
    var showTotalSpend by remember { mutableStateOf(AppearancePreferences.widgetSpendState.value) }
    var showUpcomingAlerts by remember { mutableStateOf(AppearancePreferences.widgetAlertsState.value) }

    // Consistent across all screens:
    val activeAccent = AzurePrimary
    val activeBtnBg = AzureButtonContainer

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: DESIGN
            Text(
                text = "Design",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = activeAccent
            )

            // Theme Palette Card
            PremiumSurface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Theme Accent", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Applies chosen color across the entire application",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3x3 Theme Color Grid
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PRESET_THEME_COLORS.chunked(3).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                rowItems.forEach { item ->
                                    val isSelected = selectedColorId == item.id

                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                            .border(
                                                width = if (isSelected) 2.5.dp else 1.dp,
                                                color = if (isSelected) (if (item.id == "black" && uiState.themeMode == ThemeMode.DARK) Color.White else item.color) else Color.Transparent,
                                                shape = RoundedCornerShape(18.dp)
                                            )
                                            .clickable {
                                                selectedColorId = item.id
                                                AppearancePreferences.saveColorId(item.id)
                                                Toast.makeText(context, "${item.name} applied!", Toast.LENGTH_SHORT).show()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(item.color),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Dark Mode Selector Card
            PremiumSurface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.DarkMode,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Dark mode", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Choose your contrast preference", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("Light", "Dark", "System").forEach { label ->
                            val modeEnum = when (label) {
                                "Light" -> ThemeMode.LIGHT
                                "Dark" -> ThemeMode.DARK
                                else -> ThemeMode.SYSTEM
                            }
                            val isSelected = uiState.themeMode == modeEnum

                            val btnBg = if (isSelected) activeBtnBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            val btnText = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(btnBg)
                                    .clickable { viewModel.onThemeChange(modeEnum) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = btnText,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: COMPONENTS
            Text(
                text = "Components",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = activeAccent
            )

            PremiumSurface(
                onClick = {
                    widgetOpacity = AppearancePreferences.widgetOpacityState.floatValue
                    showTotalSpend = AppearancePreferences.widgetSpendState.value
                    showUpcomingAlerts = AppearancePreferences.widgetAlertsState.value
                    showWidgetSheet = true
                },
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Outlined.Widgets,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Dashboard & Widget customization", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Controls opacity & visibility of Dashboard glance cards", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Sheet
    if (showWidgetSheet) {
        Dialog(
            onDismissRequest = { showWidgetSheet = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shadowElevation = 20.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(22.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Dashboard & Widget Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Real-time sync: Changes affect both home-screen glance and app Dashboard cards",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Live Card Preview
                    val previewCardColor = if (selectedColorId == "black") Color(0xFF1E293B) else activeAccent
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = previewCardColor.copy(alpha = widgetOpacity),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ESTIMATED MONTHLY SPEND", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Bold)
                                Text("Active", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (showTotalSpend) "INR 2,676.00" else "••••••••",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            if (showUpcomingAlerts) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("• Netflix: INR 299 (in 2d)", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                            }
                        }
                    }

                    // Opacity Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Card Background Opacity", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("${(widgetOpacity * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = activeAccent, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = widgetOpacity,
                            onValueChange = { widgetOpacity = it },
                            valueRange = 0.2f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = activeAccent, activeTrackColor = activeAccent)
                        )
                    }

                    // Spend Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Total Monthly Burn Amount", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = showTotalSpend,
                            onCheckedChange = { showTotalSpend = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = activeBtnBg)
                        )
                    }

                    // Upcoming Renewals Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Upcoming Renewals List", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = showUpcomingAlerts,
                            onCheckedChange = { showUpcomingAlerts = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = activeBtnBg)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        PremiumButton(
                            onClick = {
                                AppearancePreferences.saveWidgetSettings(widgetOpacity, showTotalSpend, showUpcomingAlerts)
                                Toast.makeText(context, "Dashboard & Widget updated!", Toast.LENGTH_SHORT).show()
                                showWidgetSheet = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = activeBtnBg, contentColor = Color.White)
                        ) {
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}