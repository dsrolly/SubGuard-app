package com.davinder.subguard.data.local

import android.content.Context
import com.davinder.subguard.data.repository.FirestoreBillingHistoryRepository
import com.davinder.subguard.data.repository.FirestoreSubscriptionRepository
import com.davinder.subguard.notification.ReminderScheduler
import kotlinx.coroutines.flow.first

/**
 * When a guest signs in, their on-device data is uploaded to their account so
 * nothing is lost. Local guest data is only wiped AFTER every upload succeeded.
 * Returns true on success (or if there was nothing to move).
 */
object GuestMigration {

    suspend fun migrateToCloud(context: Context): Boolean {
        return try {
            val guestSubscriptions = GuestSubscriptionStore(context)
            val guestBilling = GuestBillingHistoryStore(context)
            val cloudSubscriptions = FirestoreSubscriptionRepository()
            val cloudBilling = FirestoreBillingHistoryRepository()

            val subscriptions = guestSubscriptions.observeSubscriptions().first()
            val history = guestBilling.observeEntries().first()

            // Cloud documents get NEW ids, so remember old -> new to re-link billing history.
            val idMap = mutableMapOf<String, String>()
            subscriptions.forEach { sub ->
                val newId = cloudSubscriptions.addSubscription(sub.copy(id = ""))
                idMap[sub.id] = newId
            }
            history.forEach { entry ->
                cloudBilling.addEntry(
                    entry.copy(id = "", subscriptionId = idMap[entry.subscriptionId] ?: entry.subscriptionId)
                )
            }

            // Reminders were queued under the old guest ids — move them to the new ids.
            subscriptions.forEach { sub ->
                ReminderScheduler.cancelReminders(context, sub.id)
                idMap[sub.id]?.let { ReminderScheduler.scheduleReminders(context, sub.copy(id = it)) }
            }

            guestSubscriptions.clear()
            guestBilling.clear()
            true
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }
}