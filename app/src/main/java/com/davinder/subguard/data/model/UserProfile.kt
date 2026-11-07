package com.davinder.subguard.data.model

data class UserProfile(
    val uid: String = "",
    val phoneNumber: String = "",   // "+91 9876543210"  (country code, one space, number)
    val email: String = "",
    val dateOfBirth: String = "",   // "YYYY-MM-DD"
    val gender: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)