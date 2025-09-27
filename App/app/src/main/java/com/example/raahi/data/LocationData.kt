package com.example.raahi.data

import com.google.firebase.Timestamp

data class LocationData(
    val id: String = "",
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Timestamp = Timestamp.now(),
    val isEmergency: Boolean = false,
    val accuracy: Float = 0f,
    val address: String = "",
    val isSynced: Boolean = false
)

data class CachedLocationData(
    val locationData: LocationData,
    val cacheTime: Long = System.currentTimeMillis()
)
