package com.davinder.subguard.util

import android.net.Uri

/**
 * Decides whether a subscription is an ONLINE service (cancel guide shown) or an
 * OFFLINE one (gym, rent, insurance, tuition... -> no cancel guide).
 */
object CancelGuide {

    private val offlineCategories = setOf("fitness", "utilities")

    private val offlineKeywords = listOf(
        "gym", "rent", "insurance", "tuition", "school", "college", "milk", "newspaper",
        "maintenance", "society", "emi", "loan", "club", "yoga", "salon", "parking",
        "electricity", "water", "gas", "cylinder", "maid", "driver", "coaching", "hostel", "pg"
    )

    // Fitness / utility apps that ARE online even though the category looks offline.
    private val onlineKeywords = listOf(
        "cult", "headspace", "calm", "strava", "peloton", "fitbit", "myfitnesspal", "healthifyme",
        "jio", "airtel", "netflix", "spotify", "hotstar", "prime", "youtube", "apple", "google",
        "icloud", "dropbox", "chatgpt", "claude", "canva", "adobe", "microsoft", "zee5", "sonyliv", "linkedin"
    )

    fun isOnlineService(name: String, category: String): Boolean {
        val n = name.trim().lowercase()
        val c = category.trim().lowercase()
        if (onlineKeywords.any { n.contains(it) }) return true
        if (offlineKeywords.any { kw -> n.split(" ", "-", "_", ".").any { it == kw } || (kw.length > 3 && n.contains(kw)) }) return false
        if (c in offlineCategories) return false
        return true
    }

    /** Google search that shows step-by-step cancel instructions for this service. */
    fun cancelSearchUrl(name: String): String =
        "https://www.google.com/search?q=" + Uri.encode("how to cancel $name subscription")
}
