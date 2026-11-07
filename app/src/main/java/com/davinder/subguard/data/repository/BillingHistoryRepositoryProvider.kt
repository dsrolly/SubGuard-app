package com.davinder.subguard.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.davinder.subguard.data.local.GuestBillingHistoryStore

object BillingHistoryRepositoryProvider {
    fun get(context: Context): BillingHistoryRepository {
        return if (FirebaseAuth.getInstance().currentUser != null) {
            FirestoreBillingHistoryRepository()
        } else {
            GuestBillingHistoryStore(context.applicationContext)
        }
    }
}