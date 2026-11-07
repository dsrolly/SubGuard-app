package com.davinder.subguard.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.davinder.subguard.data.model.Subscription
import com.davinder.subguard.util.toLocalDateOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import java.util.concurrent.TimeUnit

/**
 * Queues four reminders per subscription (7, 3, 1 days before and on the day).
 * ALL of them are always queued — the user's Notification settings decide at
 * fire time which ones actually show (see ReminderWorker). That way changing
 * a setting takes effect immediately, without rescheduling anything.
 * ExistingWorkPolicy.REPLACE keeps re-saves from creating duplicates.
 */
object ReminderScheduler {

    private val offsets = listOf(
        Triple(7, "in 7 days", "reminder_7"),
        Triple(3, "in 3 days", "reminder_3"),
        Triple(1, "tomorrow", "reminder_1"),
        Triple(0, "today", "reminder_0")
    )

    fun scheduleReminders(context: Context, subscription: Subscription) {
        val renewalDate = subscription.nextRenewalDate.toLocalDateOrNull() ?: return
        val zone = TimeZone.currentSystemDefault()
        val workManager = WorkManager.getInstance(context)

        offsets.forEach { (daysBefore, label, tag) ->
            val fireDate = renewalDate.minus(DatePeriod(days = daysBefore))
            val delayMillis = fireDate.atStartOfDayIn(zone).toEpochMilliseconds() -
                    Clock.System.now().toEpochMilliseconds()

            // A reminder whose time already passed (e.g. renewal is only 2 days
            // away, so "7 days before" is in the past) is simply skipped.
            if (delayMillis < 0) return@forEach

            val data = Data.Builder()
                .putString("subscriptionId", subscription.id)
                .putString("subscriptionName", subscription.name)
                .putString("category", subscription.category)
                .putDouble("amount", subscription.amount)
                .putString("currency", subscription.currency)
                .putString("label", label)
                .putInt("daysBefore", daysBefore)
                .build()

            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            workManager.enqueueUniqueWork("${subscription.id}_$tag", ExistingWorkPolicy.REPLACE, request)
        }
    }

    fun cancelReminders(context: Context, subscriptionId: String) {
        val workManager = WorkManager.getInstance(context)
        offsets.forEach { (_, _, tag) -> workManager.cancelUniqueWork("${subscriptionId}_$tag") }
    }
}