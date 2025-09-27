package com.example.raahi.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.raahi.MainActivity
import com.example.raahi.R
import com.example.raahi.data.DangerZone
import com.example.raahi.data.GeofenceAlert
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.math.*

class GeofencingService(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var dangerZones = listOf<DangerZone>()
    private var isMonitoring = false
    private var monitoringJob: Job? = null
    private val lastAlertedZones = mutableSetOf<String>()

    // Add current location tracking
    private var currentLocation: GeoPoint? = null

    companion object {
        private const val TAG = "GeofencingService"
        private const val LOCATION_CHECK_INTERVAL = 10000L // 10 seconds
        private const val EARTH_RADIUS_METERS = 6371000.0
        private const val CHANNEL_ID = "geofence_alerts"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
        loadDangerZones()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Geofence Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for dangerous area alerts"
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun loadDangerZones() {
        coroutineScope.launch {
            try {
                Log.d(TAG, "🔍 Loading danger zones from Firebase...")
                Log.d(TAG, "🔑 Current user: ${auth.currentUser?.uid}")

                // First, let's try to get ALL documents in the danger collection (without filter)
                val allSnapshot = firestore.collection("danger")
                    .get()
                    .await()

                Log.d(TAG, "📊 Firebase query (ALL) returned ${allSnapshot.documents.size} documents")

                allSnapshot.documents.forEach { doc ->
                    Log.d(TAG, "📄 Document ID: ${doc.id}")
                    Log.d(TAG, "📄 Document data: ${doc.data}")
                    Log.d(TAG, "📄 isActive field: ${doc.get("isActive")} (${doc.get("isActive")?.javaClass?.simpleName})")
                }

                // Now try the filtered query
                val snapshot = firestore.collection("danger")
                    .whereEqualTo("isActive", true)
                    .get()
                    .await()

                Log.d(TAG, "📊 Firebase query (filtered) returned ${snapshot.documents.size} documents")

                dangerZones = snapshot.documents.mapNotNull { doc ->
                    try {
                        val dangerZone = DangerZone(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            location = doc.getGeoPoint("location") ?: GeoPoint(0.0, 0.0),
                            radius_meters = doc.getLong("radius_meters")?.toInt() ?: 0,
                            alert_message = doc.getString("alert_message") ?: "",
                            isActive = doc.getBoolean("isActive") ?: true
                        )
                        Log.d(TAG, "✅ Loaded danger zone: ${dangerZone.name} at ${dangerZone.location.latitude}, ${dangerZone.location.longitude} (radius: ${dangerZone.radius_meters}m)")
                        dangerZone
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error parsing danger zone document: ${doc.id}", e)
                        null
                    }
                }

                Log.d(TAG, "🎯 Successfully loaded ${dangerZones.size} danger zones")
            } catch (e: Exception) {
                Log.e(TAG, "💥 Error loading danger zones from Firebase", e)
                e.printStackTrace()
            }
        }
    }

    fun startGeofenceMonitoring() {
        if (isMonitoring) {
            Log.d(TAG, "Geofence monitoring already active")
            return
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted for geofencing")
            return
        }

        isMonitoring = true
        monitoringJob = coroutineScope.launch {
            Log.d(TAG, "Started geofence monitoring")

            while (isActive && isMonitoring) {
                try {
                    // Reload danger zones periodically (every 5 minutes)
                    if (System.currentTimeMillis() % (5 * 60 * 1000) < LOCATION_CHECK_INTERVAL) {
                        loadDangerZones()
                    }

                    // Check current location against danger zones
                    checkGeofences()

                    delay(LOCATION_CHECK_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in geofence monitoring loop", e)
                    delay(LOCATION_CHECK_INTERVAL) // Continue even if there's an error
                }
            }
        }
    }

    fun stopGeofenceMonitoring() {
        isMonitoring = false
        monitoringJob?.cancel()
        lastAlertedZones.clear()
        Log.d(TAG, "Stopped geofence monitoring")
    }

    // Add method to update location directly from GPS
    fun updateCurrentLocation(location: Location) {
        currentLocation = GeoPoint(location.latitude, location.longitude)
        Log.d(TAG, "Location updated: ${location.latitude}, ${location.longitude}")

        // Immediately check geofences when location updates
        coroutineScope.launch {
            checkGeofencesWithLocation(currentLocation!!)
        }
    }

    // Add method to check geofences with a specific location
    private suspend fun checkGeofencesWithLocation(userLocation: GeoPoint) {
        try {
            Log.d(TAG, "Checking location: ${userLocation.latitude}, ${userLocation.longitude} against ${dangerZones.size} danger zones")

            // Check each danger zone
            dangerZones.forEach { dangerZone ->
                val distance = calculateDistance(userLocation, dangerZone.location)
                Log.d(TAG, "Distance to ${dangerZone.name}: ${distance.toInt()}m (radius: ${dangerZone.radius_meters}m)")

                if (distance <= dangerZone.radius_meters) {
                    // User is inside danger zone
                    if (!lastAlertedZones.contains(dangerZone.id)) {
                        // First time entering this zone - send alert
                        sendGeofenceAlert(dangerZone, userLocation, distance)
                        lastAlertedZones.add(dangerZone.id)
                        Log.d(TAG, "🚨 User entered danger zone: ${dangerZone.name}")
                    } else {
                        Log.d(TAG, "User still in danger zone: ${dangerZone.name} (already alerted)")
                    }
                } else {
                    // User is outside danger zone
                    if (lastAlertedZones.contains(dangerZone.id)) {
                        // User has left the danger zone
                        lastAlertedZones.remove(dangerZone.id)
                        Log.d(TAG, "User left danger zone: ${dangerZone.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking geofences with location", e)
        }
    }

    private suspend fun checkGeofences() {
        try {
            val currentUser = auth.currentUser ?: return

            // Get current location from Firebase (updated by EmergencyLocationService)
            val touristDoc = firestore.collection("tourist")
                .document(currentUser.uid)
                .get()
                .await()

            val locationMap = touristDoc.get("location") as? Map<String, Any> ?: return
            val latitude = (locationMap["latitude"] as? Number)?.toDouble() ?: return
            val longitude = (locationMap["longitude"] as? Number)?.toDouble() ?: return

            val userLocation = GeoPoint(latitude, longitude)
            checkGeofencesWithLocation(userLocation)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking geofences", e)
        }
    }

    private fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Double {
        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLatRad = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLngRad = Math.toRadians(point2.longitude - point1.longitude)

        val a = sin(deltaLatRad / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) * sin(deltaLngRad / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }

    private fun sendGeofenceAlert(dangerZone: DangerZone, userLocation: GeoPoint, distance: Double) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID + dangerZone.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("⚠️ Danger Zone Alert")
                .setContentText("${dangerZone.name}: ${dangerZone.alert_message}")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("${dangerZone.alert_message}\n\nLocation: ${dangerZone.name}\nDistance: ${distance.toInt()}m from center"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500, 200, 500))
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID + dangerZone.id.hashCode(), notification)

            Log.d(TAG, "Sent geofence alert for zone: ${dangerZone.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending geofence notification", e)
        }
    }

    fun refreshDangerZones() {
        loadDangerZones()
    }

    fun getCurrentDangerZones(): List<DangerZone> = dangerZones.toList()

    fun cleanup() {
        stopGeofenceMonitoring()
        coroutineScope.cancel()
    }
}
