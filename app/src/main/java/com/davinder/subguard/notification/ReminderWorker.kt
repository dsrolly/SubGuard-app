package com.davinder.subguard.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.davinder.subguard.MainActivity
import com.davinder.subguard.data.local.PreferencesManager
import com.davinder.subguard.data.model.BillingHistoryEntry
import com.davinder.subguard.data.repository.BillingHistoryRepositoryProvider
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Builds and shows one renewal-reminder notification. Tapping it opens
 * MainActivity with the subscription's id as an extra, which the NavGraph
 * reads to deep-link straight into Subscription Detail.
 *
 * On the day-of reminder specifically, this also logs a BillingHistoryEntry —
 * that's what feeds the Analytics screen's trend chart and history list.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val subscriptionId = inputData.getString("subscriptionId") ?: return Result.failure()
        val name = inputData.getString("subscriptionName") ?: "Subscription"
        val category = inputData.getString("category") ?: "Other"
        val amount = inputData.getDouble("amount", 0.0)
        val currency = inputData.getString("currency") ?: ""
        val label = inputData.getString("label") ?: "soon"

        // Work queued by older app versions has no "daysBefore" — derive it from the label.
        val daysBefore = inputData.getInt("daysBefore", -1).let { stored ->
            if (stored >= 0) stored else when (label) { "today" -> 0; "tomorrow" -> 1; else -> 3 }
        }

        val preferences = PreferencesManager(applicationContext)
        val notificationsEnabled = preferences.notificationsEnabled.first()
        val wantedOffsets = preferences.reminderOffsets.first()

        // Only show the notification if the user's settings ask for this one.
        if (notificationsEnabled && daysBefore in wantedOffsets) {
            showNotification(subscriptionId, name, amount, currency, label)
        }

        // The billing-history record on renewal day is bookkeeping, NOT a
        // notification — it's written regardless of notification settings.
        if (daysBefore == 0) {
            BillingHistoryRepositoryProvider.get(applicationContext).addEntry(
                BillingHistoryEntry(
                    subscriptionId = subscriptionId,
                    subscriptionName = name,
                    category = category,
                    amount = amount,
                    currency = currency,
                    billedOn = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
                )
            )
        }

        return Result.success()
    }

    private fun showNotification(
        subscriptionId: String,
        name: String,
        amount: Double,
        currency: String,
        label: String
    ) {
        createChannelIfNeeded()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("subscriptionId", subscriptionId)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            subscriptionId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: swap for the app's own icon
            .setContentTitle("Upcoming renewal")
            .setContentText("$name renews $label for $currency $amount")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(subscriptionId.hashCode(), notification)
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Renewal reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts before a subscription renews"
            }
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "subguard_renewal_reminders"
    }
}