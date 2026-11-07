package com.davinder.subguard.data.model

enum class BillingCycle { WEEKLY, MONTHLY, QUARTERLY, YEARLY }
enum class SubscriptionStatus { ACTIVE, PAUSED }

/**
 * Dates are stored as ISO strings ("2026-09-25") rather than a platform-specific
 * date type — this keeps the same shape working identically whether the data
 * came from Firestore or from the guest-mode local JSON store.
 */
data class Subscription(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val currency: String = "INR",
    val category: String = "Other",
    val billingCycle: BillingCycle = BillingCycle.MONTHLY,
    val isTrial: Boolean = false,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val startDate: String = "",
    val nextRenewalDate: String = "",
    /** Per-subscription override — lets one item's reminders be paused without touching global Settings. */
    val remindersEnabled: Boolean = true
)