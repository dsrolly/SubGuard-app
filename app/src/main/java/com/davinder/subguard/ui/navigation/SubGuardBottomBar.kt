package com.davinder.subguard.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.davinder.subguard.ui.theme.AzureHeroGradient
import com.davinder.subguard.ui.theme.AzurePrimary
import com.davinder.subguard.ui.theme.OnAzureButtonContainer
import com.davinder.subguard.ui.components.rememberHaptic

/**
 * Custom Shape that carves a smooth circular cradle notch in the center
 * to nest the floating round '+' action button.
 */
class CircularCradleShape(
    private val buttonRadius: Float = 38f,
    private val cornerRadius: Float = 56f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cx = size.width / 2f
        val r = buttonRadius * density.density
        val cr = cornerRadius * density.density

        val path = Path().apply {
            reset()
            moveTo(0f, cr)
            quadraticBezierTo(0f, 0f, cr, 0f)

            // Flat edge to notch entry
            lineTo(cx - r * 1.55f, 0f)

            // Smooth circular scoop contour
            cubicTo(
                cx - r * 1.05f, 0f,
                cx - r * 0.95f, r * 0.98f,
                cx, r * 1.05f
            )
            cubicTo(
                cx + r * 0.95f, r * 0.98f,
                cx + r * 1.05f, 0f,
                cx + r * 1.55f, 0f
            )

            // Flat edge to right corner
            lineTo(size.width - cr, 0f)
            quadraticBezierTo(size.width, 0f, size.width, cr)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

sealed class BottomTab(
    val route: String,
    val title: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Home : BottomTab(Screen.Dashboard.route, "Home", Icons.Outlined.Home, Icons.Rounded.Home)
    object Calendar : BottomTab(Screen.Calendar.route, "Calendar", Icons.Outlined.CalendarToday, Icons.Rounded.CalendarToday)
    object Analytics : BottomTab(Screen.Analytics.route, "Analytics", Icons.Outlined.BarChart, Icons.Rounded.BarChart)
    object Settings : BottomTab(Screen.Settings.route, "Settings", Icons.Outlined.Settings, Icons.Rounded.Settings)
}

@Composable
fun SubGuardBottomBar(
    navController: NavController,
    currentRoute: String?,
    onAddClick: () -> Unit
) {
    val haptic = rememberHaptic()
    val barColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Scalloped / Cradle Background Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircularCradleShape(),
                    ambientColor = Color.Black.copy(alpha = 0.2f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                )
                .clip(CircularCradleShape())
                .background(barColor)
                .border(1.dp, com.davinder.subguard.ui.components.glassBorderBrush(), CircularCradleShape())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Tabs
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TabIconItem(
                        tab = BottomTab.Home,
                        isSelected = currentRoute == BottomTab.Home.route,
                        onClick = { navigateTab(navController, currentRoute, BottomTab.Home.route) }
                    )
                    TabIconItem(
                        tab = BottomTab.Calendar,
                        isSelected = currentRoute == BottomTab.Calendar.route,
                        onClick = { navigateTab(navController, currentRoute, BottomTab.Calendar.route) }
                    )
                }

                // Cradle Notch clearance gap
                Spacer(modifier = Modifier.width(76.dp))

                // Right Tabs
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TabIconItem(
                        tab = BottomTab.Analytics,
                        isSelected = currentRoute == BottomTab.Analytics.route,
                        onClick = { navigateTab(navController, currentRoute, BottomTab.Analytics.route) }
                    )
                    TabIconItem(
                        tab = BottomTab.Settings,
                        isSelected = currentRoute == BottomTab.Settings.route,
                        onClick = { navigateTab(navController, currentRoute, BottomTab.Settings.route) }
                    )
                }
            }
        }

        // Floating Center Circular Button nestled in Cradle
        Box(
            modifier = Modifier
                .offset(y = (-20).dp)
                .size(54.dp)
                .shadow(14.dp, CircleShape, spotColor = AzurePrimary.copy(alpha = 0.6f))
                .clip(CircleShape)
                .background(AzureHeroGradient)
                .clickable { haptic(); onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Subscription",
                tint = OnAzureButtonContainer,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun TabIconItem(
    tab: BottomTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val haptic = rememberHaptic()
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) AzurePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        label = "tab_color"
    )

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { haptic(); onClick() }
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
            contentDescription = tab.title,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = tab.title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

private fun navigateTab(navController: NavController, currentRoute: String?, targetRoute: String) {

    if (currentRoute == targetRoute) return

    navController.navigate(targetRoute) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}