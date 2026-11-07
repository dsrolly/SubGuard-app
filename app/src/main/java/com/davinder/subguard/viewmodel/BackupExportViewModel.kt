package com.davinder.subguard.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.davinder.subguard.data.model.BillingCycle
import com.davinder.subguard.data.model.Subscription
import com.davinder.subguard.data.model.SubscriptionStatus
import com.davinder.subguard.data.repository.SubscriptionRepositoryProvider
import com.davinder.subguard.notification.ReminderScheduler
import com.davinder.subguard.util.calculateNextRenewalDate
import com.davinder.subguard.util.subscriptionsToCsv
import com.davinder.subguard.util.toLocalDateOrNull
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

sealed class RestoreResult {
    data class Success(val added: Int, val skipped: Int) : RestoreResult()
    data class Failure(val message: String) : RestoreResult()
}

sealed class CloudSyncResult {
    data class Synced(val count: Int) : CloudSyncResult()
    /** Offline / slow network: Firestore keeps the data and uploads automatically later. */
    data class Pending(val count: Int) : CloudSyncResult()
    object NotSignedIn : CloudSyncResult()
    data class Failure(val message: String) : CloudSyncResult()
}

class BackupExportViewModel(application: Application) : AndroidViewModel(application) {

    private companion object {
        const val APP_TAG = "SubGuard"
        const val BACKUP_VERSION = 1
    }

    // Looked up on every call so it is always correct after sign-in / sign-out.
    private fun repository() = SubscriptionRepositoryProvider.get(getApplication<Application>())

    private fun newCacheFile(name: String): File {
        val dir = File(getApplication<Application>().cacheDir, "backups").apply { mkdirs() }
        return File(dir, name)
    }

    private fun dateStamp(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    /** Writes a real JSON backup of all subscriptions. Returns null if there is nothing to back up. */
    suspend fun createBackupFile(): File? = withContext(Dispatchers.IO) {
        val subscriptions = repository().observeSubscriptions().first()
        if (subscriptions.isEmpty()) return@withContext null

        val array = JSONArray()
        subscriptions.forEach { s ->
            array.put(
                JSONObject().apply {
                    put("name", s.name)
                    put("amount", s.amount)
                    put("currency", s.currency)
                    put("category", s.category)
                    put("billingCycle", s.billingCycle.name)
                    put("isTrial", s.isTrial)
                    put("startDate", s.startDate)
                    put("nextRenewalDate", s.nextRenewalDate)
                    put("status", s.status.name)
                    put("remindersEnabled", s.remindersEnabled)
                }
            )
        }
        val root = JSONObject().apply {
            put("app", APP_TAG)
            put("version", BACKUP_VERSION)
            put("exportedAt", System.currentTimeMillis())
            put("count", subscriptions.size)
            put("subscriptions", array)
        }

        newCacheFile("SubGuard_Backup_${dateStamp()}.json").also { it.writeText(root.toString(2)) }
    }

    /** Writes a real CSV file (UTF-8 with BOM so Excel shows symbols correctly). */
    suspend fun createCsvFile(): File? = withContext(Dispatchers.IO) {
        val subscriptions = repository().observeSubscriptions().first()
        if (subscriptions.isEmpty()) return@withContext null
        newCacheFile("SubGuard_Subscriptions_${dateStamp()}.csv")
            .also { file ->
                // UTF-8 BOM bytes (EF BB BF) so Excel reads the file correctly
                val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
                file.writeBytes(bom + subscriptionsToCsv(subscriptions).toByteArray(Charsets.UTF_8))
            }
    }

    /** Reads a SubGuard JSON backup and adds the subscriptions that don't already exist. */
    suspend fun restore(uri: Uri): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val text = getApplication<Application>().contentResolver
                .openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: return@withContext RestoreResult.Failure("Couldn't open the selected file")

            val root = try {
                JSONObject(text)
            } catch (_: JSONException) {
                return@withContext RestoreResult.Failure("This is not a SubGuard backup file")
            }
            if (root.optString("app") != APP_TAG) {
                return@withContext RestoreResult.Failure("This is not a SubGuard backup file")
            }
            val array = root.optJSONArray("subscriptions")
                ?: return@withContext RestoreResult.Failure("Backup file has no subscriptions")

            val repo = repository()
            val existing = repo.observeSubscriptions().first()
            val keys = existing
                .map { dedupeKey(it.name, it.amount, it.billingCycle.name) }
                .toMutableSet()
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            var added = 0
            var skipped = 0
            for (i in 0 until array.length()) {
                val o = array.optJSONObject(i) ?: continue
                val name = o.optString("name").trim()
                val amount = o.optDouble("amount", Double.NaN)
                if (name.isEmpty() || amount.isNaN() || amount <= 0) {
                    skipped++
                    continue
                }
                val cycle = runCatching { BillingCycle.valueOf(o.optString("billingCycle")) }
                    .getOrDefault(BillingCycle.MONTHLY)
                if (!keys.add(dedupeKey(name, amount, cycle.name))) {
                    skipped++
                    continue
                }
                val status = runCatching { SubscriptionStatus.valueOf(o.optString("status")) }
                    .getOrDefault(SubscriptionStatus.ACTIVE)
                val startDate = o.optString("startDate").toLocalDateOrNull() ?: today
                val nextRenewal = calculateNextRenewalDate(startDate, cycle, today)

                val subscription = Subscription(
                    id = "",
                    name = name,
                    amount = amount,
                    currency = o.optString("currency", "INR").ifBlank { "INR" },
                    category = o.optString("category", "Other").ifBlank { "Other" },
                    billingCycle = cycle,
                    isTrial = o.optBoolean("isTrial", false),
                    startDate = startDate.toString(),
                    nextRenewalDate = nextRenewal.toString(),
                    status = status,
                    remindersEnabled = o.optBoolean("remindersEnabled", true)
                )
                val newId = repo.addSubscription(subscription)
                val saved = subscription.copy(id = newId)
                if (saved.status == SubscriptionStatus.ACTIVE && saved.remindersEnabled) {
                    ReminderScheduler.scheduleReminders(getApplication(), saved)
                }
                added++
            }
            RestoreResult.Success(added, skipped)
        } catch (e: Exception) {
            RestoreResult.Failure(e.message ?: "Restore failed")
        }
    }

    /**
     * Real cloud check: waits until Firestore has uploaded every pending local write.
     * (Firestore already syncs automatically; this just confirms it.)
     */
    suspend fun syncCloud(): CloudSyncResult {
        if (FirebaseAuth.getInstance().currentUser == null) return CloudSyncResult.NotSignedIn
        return try {
            val count = withContext(Dispatchers.IO) { repository().observeSubscriptions().first().size }
            val uploaded = withTimeoutOrNull(15_000) {
                suspendCancellableCoroutine<Boolean> { cont ->
                    FirebaseFirestore.getInstance().waitForPendingWrites()
                        .addOnCompleteListener { task ->
                            if (cont.isActive) cont.resume(task.isSuccessful)
                        }
                }
            }
            when (uploaded) {
                true -> CloudSyncResult.Synced(count)
                null -> CloudSyncResult.Pending(count)
                false -> CloudSyncResult.Failure("Cloud sync failed")
            }
        } catch (e: Exception) {
            CloudSyncResult.Failure(e.message ?: "Cloud sync failed")
        }
    }

    private fun dedupeKey(name: String, amount: Double, cycle: String) =
        "${name.trim().lowercase()}|$amount|$cycle"
}