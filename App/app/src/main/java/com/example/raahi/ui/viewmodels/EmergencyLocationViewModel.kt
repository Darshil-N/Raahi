package com.example.raahi.ui.viewmodels

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.raahi.services.EmergencyLocationService
import com.example.raahi.services.EmergencyLocationForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EmergencyLocationState(
    val isTracking: Boolean = false,
    val currentLocation: Location? = null,
    val isEmergencyMode: Boolean = false,
    val cachedLocationsCount: Int = 0,
    val lastSyncTime: Long = 0,
    val isNetworkAvailable: Boolean = true,
    val permissionsGranted: Boolean = false,
    val showSOSConfirmDialog: Boolean = false,
    val sosConfirmationMessage: String? = null
)

class EmergencyLocationViewModel(application: Application) : AndroidViewModel(application) {

    private val emergencyLocationService = EmergencyLocationService(application)

    private val _uiState = MutableStateFlow(EmergencyLocationState())
    val uiState: StateFlow<EmergencyLocationState> = _uiState.asStateFlow()

    init {
        checkPermissions()
        setupLocationService()
        checkNetworkStatus()
    }

    private fun setupLocationService() {
        // Set up callback to receive location and network updates
        emergencyLocationService.setLocationUpdateCallback { location, isNetworkAvailable ->
            _uiState.value = _uiState.value.copy(
                currentLocation = location,
                isNetworkAvailable = isNetworkAvailable
            )
        }
    }

    private fun checkPermissions() {
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasSmsPermission = ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        _uiState.value = _uiState.value.copy(
            permissionsGranted = hasLocationPermission && hasSmsPermission
        )
    }

    private fun checkNetworkStatus() {
        _uiState.value = _uiState.value.copy(
            isNetworkAvailable = emergencyLocationService.isNetworkAvailable()
        )
    }

    fun startEmergencyTracking() {
        if (!_uiState.value.permissionsGranted) {
            checkPermissions() // Recheck permissions
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isTracking = true,
                    isEmergencyMode = true
                )

                // Start foreground service for reliable background tracking
                EmergencyLocationForegroundService.startService(getApplication())
                emergencyLocationService.startLocationTracking()

                // Check network status
                checkNetworkStatus()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTracking = false,
                    isEmergencyMode = false
                )
            }
        }
    }

    fun stopEmergencyTracking() {
        _uiState.value = _uiState.value.copy(
            isTracking = false,
            isEmergencyMode = false
        )

        // Stop foreground service
        EmergencyLocationForegroundService.stopService(getApplication())
        emergencyLocationService.stopLocationTracking()
    }

    fun triggerEmergencyAlert() {
        viewModelScope.launch {
            emergencyLocationService.sendImmediateEmergencyAlert()
        }
    }

    fun turnOffEmergencyState() {
        viewModelScope.launch {
            try {
                // FIXED: This now works offline and properly resets local state
                emergencyLocationService.turnOffEmergencyState()

                // Update UI state to reflect that emergency is no longer active
                _uiState.value = _uiState.value.copy(
                    isEmergencyMode = false,
                    isTracking = false // Also stop tracking when turning off emergency
                )

                // Show success message
                _uiState.value = _uiState.value.copy(
                    sosConfirmationMessage = "✅ Emergency mode turned off successfully. You are now safe."
                )
            } catch (e: Exception) {
                // Handle error - show error message to user
                _uiState.value = _uiState.value.copy(
                    sosConfirmationMessage = "❌ Failed to turn off emergency mode: ${e.message}"
                )
            }
        }
    }

    fun startBackgroundTracking() {
        if (!_uiState.value.permissionsGranted) {
            checkPermissions()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isTracking = true,
                isEmergencyMode = false
            )

            emergencyLocationService.startLocationTracking()
            checkNetworkStatus()
        }
    }

    fun stopBackgroundTracking() {
        _uiState.value = _uiState.value.copy(
            isTracking = false,
            isEmergencyMode = false
        )

        emergencyLocationService.stopLocationTracking()
    }

    fun syncCachedData() {
        viewModelScope.launch {
            try {
                emergencyLocationService.syncCachedLocations()
                _uiState.value = _uiState.value.copy(
                    lastSyncTime = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                // Handle sync error
            }
        }
    }

    fun refreshNetworkStatus() {
        checkNetworkStatus()
    }

    // Add SOS confirmation methods
    fun showSOSConfirmation() {
        _uiState.value = _uiState.value.copy(
            showSOSConfirmDialog = true,
            sosConfirmationMessage = "Are you sure you want to send an Emergency SOS alert?\n\nThis will immediately send your location to emergency services."
        )
    }

    fun confirmSOS() {
        _uiState.value = _uiState.value.copy(
            showSOSConfirmDialog = false,
            sosConfirmationMessage = null
        )

        // Actually trigger the emergency alert
        viewModelScope.launch {
            try {
                // FIXED: Set explicit emergency mode BEFORE starting tracking
                emergencyLocationService.setExplicitEmergencyMode(true)

                // Set emergency mode to true BEFORE starting tracking
                _uiState.value = _uiState.value.copy(
                    isEmergencyMode = true,
                    isTracking = true
                )

                startEmergencyTracking() // Start emergency mode
                emergencyLocationService.sendImmediateEmergencyAlert() // Send immediate alert

                _uiState.value = _uiState.value.copy(
                    sosConfirmationMessage = "🚨 Emergency SOS sent successfully! Emergency services have been notified."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    sosConfirmationMessage = "❌ Failed to send SOS: ${e.message}. Please try again or call emergency services directly."
                )
            }
        }
    }

    fun cancelSOS() {
        _uiState.value = _uiState.value.copy(
            showSOSConfirmDialog = false,
            sosConfirmationMessage = null
        )
    }

    fun clearSOSMessage() {
        _uiState.value = _uiState.value.copy(
            sosConfirmationMessage = null
        )
    }

    override fun onCleared() {
        super.onCleared()
        emergencyLocationService.cleanup()
    }
}
