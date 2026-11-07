package com.davinder.subguard.data.model

/** One record per actual charge — created when a subscription is first added,
 *  and again each time its renewal day arrives (see ReminderWorker). */
data class BillingHistoryEntry(
    val id: String = "",
    val ownerId: String = "",
    val subscriptionId: String = "",
    val subscriptionName: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val currency: String = "INR",
    val billedOn: String = "" // ISO date string, e.g. "2026-09-25"
)