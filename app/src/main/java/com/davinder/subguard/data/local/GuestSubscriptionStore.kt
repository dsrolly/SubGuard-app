package com.davinder.subguard.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.davinder.subguard.data.model.Subscription
import com.davinder.subguard.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.guestDataStore by preferencesDataStore(name = "guest_subscriptions")

/**
 * Local-only storage for guest mode (no Firebase Auth available). The whole
 * subscription list is kept as one JSON blob in DataStore — simple, and
 * plenty for the amount of data one guest user has.
 */
class GuestSubscriptionStore(private val context: Context) : SubscriptionRepository {

    private val key = stringPreferencesKey("subscriptions_json")
    private val gson = Gson()
    private val listType = object : TypeToken<List<Subscription>>() {}.type

    override fun observeSubscriptions(): Flow<List<Subscription>> =
        context.guestDataStore.data.map { prefs ->
            gson.fromJson(prefs[key] ?: "[]", listType)
        }

    override suspend fun addSubscription(subscription: Subscription): String {
        val current = readCurrent()
        val id = UUID.randomUUID().toString()
        val withId = subscription.copy(id = id)
        saveAll(current + withId)
        return id
    }

    override suspend fun updateSubscription(subscription: Subscription) {
        val current = readCurrent()
        saveAll(current.map { if (it.id == subscription.id) subscription else it })
    }

    override suspend fun deleteSubscription(subscriptionId: String) {
        val current = readCurrent()
        saveAll(current.filterNot { it.id == subscriptionId })
    }

    private suspend fun readCurrent(): List<Subscription> {
        val json = context.guestDataStore.data.first()[key] ?: "[]"
        return gson.fromJson(json, listType)
    }

    private suspend fun saveAll(list: List<Subscription>) {
        context.guestDataStore.edit { prefs -> prefs[key] = gson.toJson(list) }
    }

    /** Wipes the local guest data (called after it has been uploaded to the user's account). */
    suspend fun clear() {
        context.guestDataStore.edit { it.remove(key) }
    }
}