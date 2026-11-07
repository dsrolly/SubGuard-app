package com.davinder.subguard.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.EnterTransition
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.davinder.subguard.R
import com.davinder.subguard.ui.analytics.AnalyticsScreen
import com.davinder.subguard.ui.screens.about.AboutAppScreen
import com.davinder.subguard.ui.screens.about.WhatsNewScreen
import com.davinder.subguard.ui.screens.addeditsubscription.AddEditSubscriptionScreen
import com.davinder.subguard.ui.screens.appearance.AppearanceScreen
import com.davinder.subguard.ui.screens.backupexport.BackupExportScreen
import com.davinder.subguard.ui.screens.calendar.CalendarScreen
import com.davinder.subguard.ui.screens.currencies.CurrenciesScreen
import com.davinder.subguard.ui.screens.currencyconverter.CurrencyConverterScreen
import com.davinder.subguard.ui.screens.dashboard.DashboardScreen
import com.davinder.subguard.ui.screens.dashboarddisplay.DashboardDisplayScreen
import com.davinder.subguard.ui.screens.editprofile.EditProfileScreen
import com.davinder.subguard.ui.screens.notificationsettings.NotificationSettingsScreen
import com.davinder.subguard.ui.screens.profile.ProfileScreen
import com.davinder.subguard.ui.screens.settings.SettingsScreen
import com.davinder.subguard.ui.screens.subscriptiondetail.SubscriptionDetailScreen
import com.davinder.subguard.ui.screens.subscriptionlist.SubscriptionListScreen
import com.davinder.subguard.ui.screens.tagsmanagement.TagsManagementScreen
import com.davinder.subguard.ui.theme.AzurePrimary
import com.davinder.subguard.viewmodel.AnalyticsViewModel
import com.davinder.subguard.viewmodel.NotificationAlertsViewModel
import com.davinder.subguard.viewmodel.UpcomingRenewal
import com.google.firebase.auth.FirebaseAuth
import com.davinder.subguard.ui.screens.deleteaccount.DeleteAccountScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubGuardNavGraph(
    navController: NavHostController = rememberNavController(),
    deepLinkSubscriptionId: String? = null,
    onSignOut: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Bars (top header / bottom bar) are drawn only once every visible screen is a tab screen.
    // While a sub-screen is still fading in/out they stay invisible, so they never overlap it.
    val visibleEntries by navController.visibleEntries.collectAsState()
    val context = LocalContext.current

    var showNotificationsSheet by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }
    var permissionAsked by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        if (currentRoute == Screen.Dashboard.route &&
            !permissionAsked &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            permissionAsked = true
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    var deepLinkHandled by rememberSaveable { mutableStateOf(false) }
    val enterApp: () -> Unit = {
        navController.navigate(Screen.Dashboard.route) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
        if (!deepLinkHandled && !deepLinkSubscriptionId.isNullOrBlank()) {
            deepLinkHandled = true
            navController.navigate(Screen.SubscriptionDetail.createRoute(deepLinkSubscriptionId))
        }
    }

    val mainTabRoutes = listOf(
        Screen.Dashboard.route,
        Screen.Calendar.route,
        Screen.Analytics.route,
        Screen.Settings.route
    )
    val barsSettled = visibleEntries.all { it.destination.route in mainTabRoutes }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val initials = remember(currentUser) {
        val name = currentUser?.displayName.orEmpty()
        val email = currentUser?.email.orEmpty()
        when {
            name.isNotBlank() -> name.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .map { it.first().uppercase() }
                .joinToString("")
            email.isNotBlank() -> email.take(2).uppercase()
            else -> ""
        }
    }

    // Real renewal alerts (filtered by the user's Notification Settings).
    // key = uid so a different account never sees the previous user's alerts.
    val alertsViewModel: NotificationAlertsViewModel =
        viewModel(key = currentUser?.uid ?: "guest")
    val alerts by alertsViewModel.alerts.collectAsState()
    var readKeys by remember { mutableStateOf(setOf<String>()) }
    val unreadAlerts = alerts.filter { it.alertKey() !in readKeys }
    val hasAlerts = unreadAlerts.isNotEmpty()

    Scaffold(
        topBar = {
            if (currentRoute in mainTabRoutes) {
                FadeBar(barsSettled) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo + App Name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_subguard_logo),
                                contentDescription = "SubGuard Logo",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(11.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "SubGuard",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Top-Right Icons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. Notification Bell
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .border(1.dp, com.davinder.subguard.ui.components.glassBorderBrush(), CircleShape)
                                    .clickable { showNotificationsSheet = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )

                                if (hasAlerts) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-8).dp, y = 8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444))
                                    )
                                }
                            }

                            // 2. Profile Avatar: Visible on Dashboard, Calendar & Analytics ONLY (Hidden in Settings)
                            if (currentRoute != Screen.Settings.route) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .border(1.dp, com.davinder.subguard.ui.components.glassBorderBrush(), CircleShape)
                                        .clickable { navController.navigate(Screen.Profile.route) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (currentUser != null && initials.isNotBlank()) {
                                        Text(
                                            text = initials,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.Person,
                                            contentDescription = "Profile",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (currentRoute in mainTabRoutes) {
                FadeBar(barsSettled) {
                    SubGuardBottomBar(
                        navController = navController,
                        currentRoute = currentRoute,
                        onAddClick = {
                            navController.navigate(Screen.AddEditSubscription.createRoute())
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = if (currentRoute in mainTabRoutes) 0.dp else padding.calculateBottomPadding()
                )
        ) {
            // Fade-through transitions: the old screen fades OUT first, then the new one fades IN.
            // They never overlap, so two screens can no longer "merge" for a moment.
            // Switching between the 4 main tabs is instant.
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                enterTransition = {
                    if (initialState.destination.route in mainTabRoutes && targetState.destination.route in mainTabRoutes)
                        EnterTransition.None
                    else fadeIn(tween(170, delayMillis = 100))
                },
                exitTransition = {
                    if (initialState.destination.route in mainTabRoutes && targetState.destination.route in mainTabRoutes)
                        ExitTransition.None
                    else fadeOut(tween(100))
                },
                popEnterTransition = {
                    if (initialState.destination.route in mainTabRoutes && targetState.destination.route in mainTabRoutes)
                        EnterTransition.None
                    else fadeIn(tween(170, delayMillis = 100))
                },
                popExitTransition = {
                    if (initialState.destination.route in mainTabRoutes && targetState.destination.route in mainTabRoutes)
                        ExitTransition.None
                    else fadeOut(tween(100))
                }
            ) {
                authGraph(
                    navController = navController,
                    enterApp = enterApp
                )

                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        onViewAllClick = { navController.navigate(Screen.SubscriptionList.route) },
                        onSubscriptionClick = { id ->
                            navController.navigate(Screen.SubscriptionDetail.createRoute(id))
                        },
                        onConverterClick = { navController.navigate(Screen.CurrencyConverter.route) }
                    )
                }

                composable(Screen.Calendar.route) {
                    CalendarScreen(
                        onSubscriptionClick = { id ->
                            navController.navigate(Screen.SubscriptionDetail.createRoute(id))
                        }
                    )
                }

                composable(Screen.Analytics.route) {
                    // key = uid: har account / guest ka apna alag AnalyticsViewModel,
                    // taaki account badalne par purana data kabhi na dikhe.
                    val analyticsViewModel: AnalyticsViewModel =
                        viewModel(key = "analytics_" + (currentUser?.uid ?: "guest"))
                    AnalyticsScreen(viewModel = analyticsViewModel)
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        onNavigateToCurrencies = { navController.navigate(Screen.Currencies.route) },
                        onNavigateToNotifications = { navController.navigate(Screen.NotificationSettings.route) },
                        onNavigateToDashboardDisplay = { navController.navigate(Screen.DashboardDisplay.route) },
                        onNavigateToAppearance = { navController.navigate(Screen.Appearance.route) },
                        onNavigateToBackup = { navController.navigate(Screen.BackupExport.route) },
                        onNavigateToTags = { navController.navigate(Screen.TagsManagement.route) },
                        onNavigateToAbout = { navController.navigate(Screen.AboutApp.route) },
                        onNavigateToWhatsNew = {
                            com.davinder.subguard.util.AppLinks.open(navController.context, com.davinder.subguard.util.AppLinks.WHATS_NEW)
                        },
                        onNavigateToDeleteAccount = { navController.navigate(Screen.DeleteAccount.route) },
                        onSignIn = { navController.navigate(Screen.SignIn.route) },
                        onSignedOut = onSignOut
                    )
                }

                composable(
                    route = Screen.AddEditSubscription.route,
                    arguments = listOf(
                        navArgument("subscriptionId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    AddEditSubscriptionScreen(
                        subscriptionId = backStackEntry.arguments?.getString("subscriptionId"),
                        onSaved = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.SubscriptionDetail.route,
                    arguments = listOf(navArgument("subscriptionId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val subscriptionId = backStackEntry.arguments?.getString("subscriptionId") ?: ""
                    SubscriptionDetailScreen(
                        subscriptionId = subscriptionId,
                        onBack = { navController.popBackStack() },
                        onEditClick = { id ->
                            navController.navigate(Screen.AddEditSubscription.createRoute(id))
                        }
                    )
                }

                composable(Screen.SubscriptionList.route) {
                    SubscriptionListScreen(
                        onSubscriptionClick = { id ->
                            navController.navigate(Screen.SubscriptionDetail.createRoute(id))
                        }
                    )
                }

                composable(Screen.CurrencyConverter.route) {
                    CurrencyConverterScreen()
                }

                composable(Screen.Currencies.route) {
                    CurrenciesScreen(
                        onSelectCurrency = {},
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.DashboardDisplay.route) {
                    DashboardDisplayScreen(onBack = { navController.popBackStack() })
                }

                composable(Screen.BackupExport.route) {
                    BackupExportScreen(onBack = { navController.popBackStack() })
                }

                composable(Screen.DeleteAccount.route) {
                    DeleteAccountScreen(
                        onBack = { navController.popBackStack() },
                        onAccountDeleted = onSignOut
                    )
                }

                composable(Screen.TagsManagement.route) {
                    TagsManagementScreen(onBack = { navController.popBackStack() })
                }

                composable(Screen.AboutApp.route) {
                    AboutAppScreen(onBack = { navController.popBackStack() })
                }

                composable(Screen.WhatsNew.route) {
                    WhatsNewScreen(onBack = { navController.popBackStack() })
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onBack = { navController.popBackStack() },
                        onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                        onSignIn = {
                            navController.navigate(Screen.SignIn.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        },
                        onSignedOut = onSignOut
                    )
                }

                composable(Screen.EditProfile.route) {
                    EditProfileScreen(
                        onBack = { navController.popBackStack() },
                        onSaved = { navController.popBackStack() }
                    )
                }

                composable(Screen.Appearance.route) {
                    AppearanceScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.NotificationSettings.route) {
                    NotificationSettingsScreen()
                }
            }
        }
    }

    if (showNotificationsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationsSheet = false },
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notifications & Alerts",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (hasAlerts) {
                        TextButton(onClick = { readKeys = readKeys + alerts.map { it.alertKey() } }) {
                            Text("Mark all read", color = AzurePrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (hasAlerts) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        unreadAlerts.forEach { alert ->
                            NotificationAlertCard(
                                serviceName = alert.subscription.name,
                                dueDate = when (alert.daysAway) {
                                    0 -> "Due today"
                                    1 -> "Due tomorrow"
                                    else -> "Due in ${alert.daysAway} days"
                                },
                                amount = "${alert.subscription.currency} ${"%.2f".format(alert.subscription.amount)}",
                                onDismiss = { readKeys = readKeys + alert.alertKey() }
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No new alerts",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "You're all caught up with your upcoming payments!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun NotificationAlertCard(
    serviceName: String,
    dueDate: String,
    amount: String,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "$serviceName Renewal",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$dueDate • $amount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** Unique per subscription AND renewal date, so "read" resets after the next billing cycle. */
private fun UpcomingRenewal.alertKey(): String =
    "${subscription.id}|${subscription.nextRenewalDate}"

/** Fades a bar in/out without changing its size, so layout never jumps. */
@Composable
private fun FadeBar(visible: Boolean, content: @Composable () -> Unit) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(if (visible) 160 else 60),
        label = "barAlpha"
    )
    Box(modifier = Modifier.graphicsLayer { this.alpha = alpha }) { content() }
}
