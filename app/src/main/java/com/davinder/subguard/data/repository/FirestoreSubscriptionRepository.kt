package com.davinder.subguard.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.davinder.subguard.data.firebase.FirestoreCollections
import com.davinder.subguard.data.model.Subscription
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestoreException

class FirestoreSubscriptionRepository : SubscriptionRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun collection() =
        firestore.collection(FirestoreCollections.SUBSCRIPTIONS)

    private val currentUserId: String
        get() = auth.currentUser?.uid
            ?: throw IllegalStateException("No signed-in user")

    override fun observeSubscriptions(): Flow<List<Subscription>> = callbackFlow {
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
                val subscriptions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Subscription::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(subscriptions)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addSubscription(subscription: Subscription): String {
        val data = subscription.copy(ownerId = currentUserId)
        val ref = collection().add(data).await()
        return ref.id
    }

    override suspend fun updateSubscription(subscription: Subscription) {
        if (subscription.id.isBlank()) return
        // Explicitly attach currentUserId so ownerId is never lost or overwritten
        val data = subscription.copy(ownerId = currentUserId)
        collection().document(subscription.id)
            .set(data, SetOptions.merge())
            .await()
    }

    override suspend fun deleteSubscription(subscriptionId: String) {
        if (subscriptionId.isNotBlank()) {
            collection().document(subscriptionId).delete().await()
        }
    }
}