package com.example.raahi.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class DangerZone(
    val id: String = "",
    val name: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val radius_meters: Int = 0,
    val alert_message: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val isActive: Boolean = true
)

data class GeofenceAlert(
    val dangerZone: DangerZone,
    val userLocation: GeoPoint,
    val distance: Double,
    val timestamp: Timestamp = Timestamp.now()
)
