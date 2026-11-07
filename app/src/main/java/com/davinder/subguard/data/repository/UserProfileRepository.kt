package com.davinder.subguard.data.repository

import com.davinder.subguard.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Firestore-only (guests never get a profile). Document id = Firebase uid.
 * Drop-in for the existing file: same three functions the app already calls,
 * with getProfile() returning null when there is no profile yet.
 */
class UserProfileRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // Same value as FirestoreCollections.USER_PROFILES
    private val collection get() = firestore.collection("userProfiles")

    private fun currentUid(): String? = FirebaseAuth.getInstance().currentUser?.uid

    suspend fun saveProfile(profile: UserProfile) {
        collection.document(profile.uid).set(profile).await()
    }

    suspend fun getProfile(): UserProfile? {
        val uid = currentUid() ?: return null
        return collection.document(uid).get().await().toObject(UserProfile::class.java)
    }

    suspend fun hasProfile(): Boolean {
        val uid = currentUid() ?: return false
        return collection.document(uid).get().await().exists()
    }
}