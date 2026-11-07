package com.davinder.subguard.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.davinder.subguard.data.local.GuestSubscriptionStore

object SubscriptionRepositoryProvider {
    fun get(context: Context): SubscriptionRepository {
        return if (FirebaseAuth.getInstance().currentUser != null) {
            FirestoreSubscriptionRepository()
        } else {
            GuestSubscriptionStore(context.applicationContext)
        }
    }
}