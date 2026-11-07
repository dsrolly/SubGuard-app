package com.davinder.subguard.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestore
import com.davinder.subguard.data.firebase.FirestoreCollections
import com.davinder.subguard.data.model.BillingHistoryEntry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreBillingHistoryRepository : BillingHistoryRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun collection() = firestore.collection(FirestoreCollections.BILLING_HISTORY)

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("No signed-in user")

    override fun observeEntries(): Flow<List<BillingHistoryEntry>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = collection()
            .whereEqualTo("ownerId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        close()
                    } else {
                        close(error)
                    }
                    return@addSnapshotListener
                }
                val entries = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(BillingHistoryEntry::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(entries)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addEntry(entry: BillingHistoryEntry) {
        val data = entry.copy(ownerId = currentUserId)
        collection().add(data).await()
    }

    override suspend fun deleteEntriesForSubscription(subscriptionId: String) {
        if (subscriptionId.isBlank()) return

        // Query all billing history entries that match this subscription ID for the current user
        val snapshot = collection()
            .whereEqualTo("ownerId", currentUserId)
            .whereEqualTo("subscriptionId", subscriptionId)
            .get()
            .await()

        if (snapshot.isEmpty) return

        // Execute batch delete to efficiently remove all matching records
        val batch = firestore.batch()
        for (doc in snapshot.documents) {
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    override suspend fun deleteEntry(entryId: String) {
        if (entryId.isBlank()) return
        collection().document(entryId).delete().await()
    }
}