package com.davinder.subguard.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object SignIn : Screen("sign_in")
    object Login : Screen("login")
    object Register : Screen("register")
    object EmailVerification : Screen("email_verification")
    object ProfileSetup : Screen("profile_setup")

    object Dashboard : Screen("dashboard")
    object Calendar : Screen("calendar")
    object Analytics : Screen("analytics")
    object Settings : Screen("settings")
    object NotificationSettings : Screen("notification_settings")
    object SubscriptionList : Screen("subscription_list")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object DeleteAccount : Screen("delete_account")

    object AddEditSubscription : Screen("add_edit_subscription?subscriptionId={subscriptionId}") {
        fun createRoute(subscriptionId: String? = null): String =
            "add_edit_subscription?subscriptionId=${subscriptionId ?: ""}"
    }

    object SubscriptionDetail : Screen("subscription_detail/{subscriptionId}") {
        fun createRoute(subscriptionId: String): String =
            "subscription_detail/$subscriptionId"
    }

    object CurrencyConverter : Screen("currency_converter")

    // --- NEW SETTINGS SCREENS ---
    object Currencies : Screen("currencies")
    object DashboardDisplay : Screen("dashboard_display")
    object BackupExport : Screen("backup_export")
    object TagsManagement : Screen("tags_management")
    object AboutApp : Screen("about_app")
    object WhatsNew : Screen("whats_new")
    object Appearance : Screen("appearance")
}