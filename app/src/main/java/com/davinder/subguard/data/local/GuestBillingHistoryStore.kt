package com.davinder.subguard.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.davinder.subguard.data.model.BillingHistoryEntry
import com.davinder.subguard.data.repository.BillingHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.guestBillingHistoryDataStore by preferencesDataStore(name = "guest_billing_history")

class GuestBillingHistoryStore(private val context: Context) : BillingHistoryRepository {

    private val key = stringPreferencesKey("billing_history_json")
    private val gson = Gson()
    private val listType = object : TypeToken<List<BillingHistoryEntry>>() {}.type

    override fun observeEntries(): Flow<List<BillingHistoryEntry>> =
        context.guestBillingHistoryDataStore.data.map { prefs ->
            gson.fromJson(prefs[key] ?: "[]", listType)
        }

    override suspend fun addEntry(entry: BillingHistoryEntry) {
        val current: List<BillingHistoryEntry> = gson.fromJson(
            context.guestBillingHistoryDataStore.data.first()[key] ?: "[]",
            listType
        )
        val withId = entry.copy(id = UUID.randomUUID().toString())
        context.guestBillingHistoryDataStore.edit { prefs ->
            prefs[key] = gson.toJson(current + withId)
        }
    }

    /**
     * Deletes all billing history records matching the given subscription ID.
     * Keeps guest analytics clean when a subscription is deleted.
     */
    override suspend fun deleteEntriesForSubscription(subscriptionId: String) {
        val current: List<BillingHistoryEntry> = gson.fromJson(
            context.guestBillingHistoryDataStore.data.first()[key] ?: "[]",
            listType
        )
        val filtered = current.filterNot { it.subscriptionId == subscriptionId }
        context.guestBillingHistoryDataStore.edit { prefs ->
            prefs[key] = gson.toJson(filtered)
        }
    }

    /**
     * Deletes a single billing history entry by its ID.
     */
    override suspend fun deleteEntry(entryId: String) {
        val current: List<BillingHistoryEntry> = gson.fromJson(
            context.guestBillingHistoryDataStore.data.first()[key] ?: "[]",
            listType
        )
        val filtered = current.filterNot { it.id == entryId }
        context.guestBillingHistoryDataStore.edit { prefs ->
            prefs[key] = gson.toJson(filtered)
        }
    }

    /**
     * Wipes the local guest data (called after it has been migrated to the user's account).
     */
    suspend fun clear() {
        context.guestBillingHistoryDataStore.edit { it.remove(key) }
    }
}