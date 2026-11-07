package com.davinder.subguard.data.repository

import com.davinder.subguard.data.model.Subscription
import kotlinx.coroutines.flow.Flow

/**
 * One contract, two implementations: FirestoreSubscriptionRepository (signed-in
 * users) and GuestSubscriptionStore (guest mode). ViewModels depend only on
 * this interface — they never know which one they're talking to.
 */
interface SubscriptionRepository {
    fun observeSubscriptions(): Flow<List<Subscription>>

    /** Returns the newly created subscription's id (needed to schedule its reminders). */
    suspend fun addSubscription(subscription: Subscription): String

    suspend fun updateSubscription(subscription: Subscription)
    suspend fun deleteSubscription(subscriptionId: String)
}