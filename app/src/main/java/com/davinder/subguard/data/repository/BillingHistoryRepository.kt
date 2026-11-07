package com.davinder.subguard.data.repository

import com.davinder.subguard.data.model.BillingHistoryEntry
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing billing history entries.
 * Implementations handle either Firestore (authenticated users) or
 * in-memory storage (guest mode).
 */
interface BillingHistoryRepository {
    /**
     * Observes real-time updates for all billing history entries belonging to the user.
     */
    fun observeEntries(): Flow<List<BillingHistoryEntry>>

    /**
     * Adds a new billing history record.
     */
    suspend fun addEntry(entry: BillingHistoryEntry)

    /**
     * Deletes all billing history entries associated with a specific subscription.
     * Used for cascading deletes when a subscription is removed.
     */
    suspend fun deleteEntriesForSubscription(subscriptionId: String)

    /**
     * Deletes a single billing history entry by its document ID.
     */
    suspend fun deleteEntry(entryId: String)
}