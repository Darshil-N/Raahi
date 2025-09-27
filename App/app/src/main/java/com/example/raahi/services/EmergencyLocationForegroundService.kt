package com.example.raahi.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.raahi.MainActivity
import com.example.raahi.R
import kotlinx.coroutines.*

class EmergencyLocationForegroundService : Service() {

    private var emergencyLocationService: EmergencyLocationService? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Add wake lock to ensure service stays alive
    private lateinit var wakeLock: android.os.PowerManager.WakeLock

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "emergency_location_channel"
        private const val TAG = "EmergencyForegroundService"

        fun startService(context: Context) {
            val intent = Intent(context, EmergencyLocationForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, EmergencyLocationForegroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        android.util.Log.d(TAG, "🔧 Emergency Foreground Service Created")

        // Acquire wake lock to prevent the service from being killed
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        wakeLock = powerManager.newWakeLock(
            android.os.PowerManager.PARTIAL_WAKE_LOCK,
            "Raahi::EmergencyLocationWakeLock"
        )

        emergencyLocationService = EmergencyLocationService(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d(TAG, "🚀 Starting foreground service for background geofencing")

        // Acquire wake lock
        if (!wakeLock.isHeld) {
            wakeLock.acquire(60*60*1000L /*60 minutes*/)
        }

        startForeground(NOTIFICATION_ID, createNotification())

        serviceScope.launch {
            emergencyLocationService?.startLocationTracking(isBackground = true) // Enable background mode
            android.util.Log.d(TAG, "✅ Background location tracking started with optimized intervals")
        }

        // START_STICKY ensures the service restarts if killed by the system
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d(TAG, "🛑 Emergency Foreground Service Destroyed")

        // Release wake lock
        if (wakeLock.isHeld) {
            wakeLock.release()
        }

        emergencyLocationService?.stopLocationTracking()
        emergencyLocationService?.cleanup()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Emergency Location Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks location for emergency purposes"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("🚨 Emergency Location Active")
        .setContentText("Raahi is tracking your location for safety")
        .setSmallIcon(android.R.drawable.ic_menu_mylocation) // Using system location icon
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()
}
