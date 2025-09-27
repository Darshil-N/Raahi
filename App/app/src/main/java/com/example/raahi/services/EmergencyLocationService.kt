package com.example.raahi.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.raahi.data.CachedLocationData
import com.example.raahi.data.LocationData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

class EmergencyLocationService(private val context: Context) : LocationListener {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val gson = Gson()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // SharedPreferences for caching
    private val prefs = context.getSharedPreferences("emergency_cache", Context.MODE_PRIVATE)

    // Emergency contacts
    private val emergencyPoliceNumbers = listOf("6353713856")

    // Network state tracking
    private var isNetworkConnected = false
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    // Add Geofencing Service
    private val geofencingService = GeofencingService(context)

    companion object {
        private const val TAG = "EmergencyLocationService"
        private const val CACHE_KEY = "cached_locations"

        // Optimize for background operation - more frequent updates for better geofencing
        private const val LOCATION_UPDATE_INTERVAL_BACKGROUND = 15000L // 15 seconds for background
        private const val LOCATION_UPDATE_INTERVAL_FOREGROUND = 30000L // 30 seconds for foreground
        private const val MIN_DISTANCE = 5f // Reduced to 5 meters for better geofence detection
    }

    // Add flag to track background mode
    private var isBackgroundMode = false

    private var locationUpdateJob: Job? = null
    private var isLocationTrackingActive = false
    private var onLocationUpdateCallback: ((Location, Boolean) -> Unit)? = null

    init {
        setupNetworkCallback()
    }

    private fun setupNetworkCallback() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isNetworkConnected = true
                Log.d(TAG, "Network available")
                coroutineScope.launch {
                    syncCachedLocations()
                }
            }

            override fun onLost(network: Network) {
                isNetworkConnected = false
                Log.d(TAG, "Network lost")
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
            isNetworkConnected = isNetworkAvailable()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback", e)
        }
    }

    fun setLocationUpdateCallback(callback: (Location, Boolean) -> Unit) {
        onLocationUpdateCallback = callback
    }

    fun startLocationTracking(isBackground: Boolean = false) {
        if (isLocationTrackingActive) {
            Log.d(TAG, "Location tracking already active")
            return
        }

        isBackgroundMode = isBackground
        val updateInterval = if (isBackground) LOCATION_UPDATE_INTERVAL_BACKGROUND else LOCATION_UPDATE_INTERVAL_FOREGROUND

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted")
            return
        }

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    updateInterval,
                    MIN_DISTANCE,
                    this
                )
                Log.d(TAG, "GPS location updates requested (${if (isBackground) "Background" else "Foreground"} mode: ${updateInterval}ms)")
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    updateInterval,
                    MIN_DISTANCE,
                    this
                )
                Log.d(TAG, "Network location updates requested (${if (isBackground) "Background" else "Foreground"} mode: ${updateInterval}ms)")
            }

            // Start geofencing monitoring
            geofencingService.startGeofenceMonitoring()

            isLocationTrackingActive = true
            Log.d(TAG, "📍 Location tracking and geofencing started in ${if (isBackground) "BACKGROUND" else "FOREGROUND"} mode")

        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception when requesting location updates", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting location tracking", e)
        }
    }

    fun stopLocationTracking() {
        if (!isLocationTrackingActive) return

        try {
            locationManager.removeUpdates(this)
            geofencingService.stopGeofenceMonitoring()
            isLocationTrackingActive = false
            locationUpdateJob?.cancel()
            Log.d(TAG, "Location tracking and geofencing stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping location tracking", e)
        }
    }

    // LocationListener implementation
    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "📍 Location changed: ${location.latitude}, ${location.longitude} (accuracy: ${location.accuracy}m)")

        // Update geofencing service with new location immediately
        geofencingService.updateCurrentLocation(location)

        coroutineScope.launch {
            try {
                val isEmergency = onLocationUpdateCallback != null
                handleLocationUpdate(location, isEmergency)

                // Notify callback
                onLocationUpdateCallback?.invoke(location, isNetworkConnected)
            } catch (e: Exception) {
                Log.e(TAG, "Error handling location update", e)
            }
        }
    }

    // Add method to force an immediate location update for testing
    fun forceLocationCheck() {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                if (lastLocation != null) {
                    Log.d(TAG, "🔧 Force checking location: ${lastLocation.latitude}, ${lastLocation.longitude}")
                    geofencingService.updateCurrentLocation(lastLocation)
                } else {
                    Log.w(TAG, "⚠️ No last known location available for force check")
                }
            } else {
                Log.w(TAG, "⚠️ Location permission not granted for force check")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in force location check", e)
        }
    }

    override fun onProviderEnabled(provider: String) {
        Log.d(TAG, "Provider enabled: $provider")
    }

    override fun onProviderDisabled(provider: String) {
        Log.d(TAG, "Provider disabled: $provider")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.d(TAG, "Provider status changed: $provider, status: $status")
    }

    suspend fun handleLocationUpdate(location: Location, isEmergency: Boolean = false) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "User not authenticated")
            return
        }

        val locationData = LocationData(
            id = UUID.randomUUID().toString(),
            userId = currentUser.uid,
            latitude = location.latitude,
            longitude = location.longitude,
            timestamp = com.google.firebase.Timestamp.now(),
            isEmergency = isEmergency,
            accuracy = location.accuracy,
            address = getAddressFromLocation(location.latitude, location.longitude),
            isSynced = false
        )

        Log.d(TAG, "Handling location update - Network: $isNetworkConnected, Emergency: $isEmergency, ExplicitMode: ${isExplicitEmergencyMode()}")

        if (isNetworkConnected) {
            try {
                sendLocationToFirebase(locationData)
                Log.d(TAG, "Location sent to Firebase successfully")
                syncCachedLocations()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send location to Firebase", e)
                cacheLocation(locationData)
                // FIXED: Only send SMS when explicitly in emergency mode AND user confirmed
                if (isEmergency && isExplicitEmergencyMode()) {
                    sendEmergencySMS(locationData)
                }
            }
        } else {
            Log.d(TAG, "Network not available, caching location")
            cacheLocation(locationData)
            // FIXED: Only send SMS when explicitly in emergency mode AND user confirmed
            if (isEmergency && isExplicitEmergencyMode()) {
                sendEmergencySMS(locationData)
            }
        }
    }

    private suspend fun sendLocationToFirebase(locationData: LocationData) {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser ?: throw Exception("User not authenticated")

                // Create GeoPoint object for proper Firestore storage
                val geoPoint = GeoPoint(locationData.latitude, locationData.longitude)

                firestore.collection("tourist")
                    .document(currentUser.uid)
                    .update(mapOf(
                        "location" to geoPoint, // Use GeoPoint instead of HashMap
                        "isInDistress" to locationData.isEmergency,
                        "lastUpdatedAt" to locationData.timestamp
                    ))
                    .await()

                Log.d(TAG, "Location sent to Firebase successfully: ${locationData.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending location to Firebase", e)
                throw e
            }
        }
    }

    private fun cacheLocation(locationData: LocationData) {
        try {
            val cachedData = CachedLocationData(locationData)
            val existingCache = getCachedLocations().toMutableList()
            existingCache.add(cachedData)

            if (existingCache.size > 100) {
                existingCache.removeAt(0)
            }

            val cacheJson = gson.toJson(existingCache)
            prefs.edit().putString(CACHE_KEY, cacheJson).apply()
            Log.d(TAG, "Location cached successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cache location", e)
        }
    }

    private fun getCachedLocations(): List<CachedLocationData> {
        return try {
            val cacheJson = prefs.getString(CACHE_KEY, "[]")
            val type = object : TypeToken<List<CachedLocationData>>() {}.type
            gson.fromJson(cacheJson, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get cached locations", e)
            emptyList()
        }
    }

    suspend fun syncCachedLocations() {
        if (!isNetworkConnected) return

        val cachedLocations = getCachedLocations()
        if (cachedLocations.isEmpty()) return

        Log.d(TAG, "Syncing ${cachedLocations.size} cached locations")

        withContext(Dispatchers.IO) {
            val successfulSyncs = mutableListOf<CachedLocationData>()

            cachedLocations.forEach { cachedData ->
                try {
                    sendLocationToFirebase(cachedData.locationData)
                    successfulSyncs.add(cachedData)
                    Log.d(TAG, "Synced cached location: ${cachedData.locationData.id}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync cached location", e)
                }
            }

            if (successfulSyncs.isNotEmpty()) {
                val remainingCache = cachedLocations.filterNot { it in successfulSyncs }
                val cacheJson = gson.toJson(remainingCache)
                prefs.edit().putString(CACHE_KEY, cacheJson).apply()
                Log.d(TAG, "Synced ${successfulSyncs.size} cached locations")
            }
        }
    }

    private fun sendEmergencySMS(locationData: LocationData) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "SMS permission not granted")
            return
        }

        try {
            val smsManager = SmsManager.getDefault()
            val message = createEmergencySMSMessage(locationData)

            emergencyPoliceNumbers.forEach { phoneNumber ->
                try {
                    val parts = smsManager.divideMessage(message)
                    if (parts.size == 1) {
                        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                    } else {
                        smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
                    }
                    Log.d(TAG, "Emergency SMS sent to $phoneNumber")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send SMS to $phoneNumber", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send emergency SMS", e)
        }
    }

    private fun createEmergencySMSMessage(locationData: LocationData): String {
        return """🚨 EMERGENCY ALERT 🚨
User: ${locationData.userId}
Location: ${locationData.latitude}, ${locationData.longitude}
Time: ${locationData.timestamp.toDate()}
Accuracy: ${locationData.accuracy}m

Maps: https://maps.google.com/?q=${locationData.latitude},${locationData.longitude}

From Raahi App""".trimIndent()
    }

    fun isNetworkAvailable(): Boolean {
        return try {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network availability", e)
            false
        }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        return "Lat: $latitude, Lng: $longitude"
    }

    // Add method to check if we're in explicit emergency mode (user confirmed SOS)
    private fun isExplicitEmergencyMode(): Boolean {
        return prefs.getBoolean("explicit_emergency_mode", false)
    }

    // Add public method to set explicit emergency mode
    fun setExplicitEmergencyMode(isEmergency: Boolean) {
        prefs.edit().putBoolean("explicit_emergency_mode", isEmergency).apply()
        Log.d(TAG, "Explicit emergency mode set to: $isEmergency")
    }

    suspend fun turnOffEmergencyState() {
        try {
            // FIXED: Always turn off local emergency state first (works offline)
            setExplicitEmergencyMode(false)

            // Stop location tracking and geofencing
            stopLocationTracking()

            Log.d(TAG, "Local emergency state turned off successfully")

            // Try to update Firebase if online
            val currentUser = auth.currentUser
            if (currentUser != null && isNetworkConnected) {
                try {
                    firestore.collection("tourist")
                        .document(currentUser.uid)
                        .update(mapOf(
                            "isInDistress" to false,
                            "lastUpdatedAt" to com.google.firebase.Timestamp.now()
                        ))
                        .await()
                    Log.d(TAG, "Firebase emergency state turned off successfully")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to update Firebase (will try later when online)", e)
                    // Cache the turn-off request for later sync
                    prefs.edit().putBoolean("pending_emergency_off", true).apply()
                }
            } else {
                // Cache the turn-off request for later sync when network is available
                prefs.edit().putBoolean("pending_emergency_off", true).apply()
                Log.d(TAG, "Emergency state turn-off cached for later sync")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error turning off emergency state", e)
            throw e
        }
    }

    suspend fun sendImmediateEmergencyAlert() {
        try {
            val lastKnownLocation = if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else null

            if (lastKnownLocation != null) {
                Log.d(TAG, "Sending immediate emergency alert with last known location")
                handleLocationUpdate(lastKnownLocation, isEmergency = true)
            } else {
                Log.w(TAG, "No location available for emergency alert")
                val emergencyData = LocationData(
                    id = UUID.randomUUID().toString(),
                    userId = auth.currentUser?.uid ?: "unknown",
                    latitude = 0.0,
                    longitude = 0.0,
                    timestamp = com.google.firebase.Timestamp.now(),
                    isEmergency = true,
                    accuracy = 0f,
                    address = "Location unavailable",
                    isSynced = false
                )
                // FIXED: Only send SMS if explicitly in emergency mode
                if (isExplicitEmergencyMode()) {
                    sendEmergencySMS(emergencyData)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending immediate emergency alert", e)
        }
    }

    // Add public method to refresh danger zones
    fun refreshDangerZones() {
        geofencingService.refreshDangerZones()
    }

    fun cleanup() {
        stopLocationTracking()
        geofencingService.cleanup()
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
        }
        coroutineScope.cancel()
    }
}
