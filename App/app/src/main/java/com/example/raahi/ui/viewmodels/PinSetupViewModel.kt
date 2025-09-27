package com.example.raahi.ui.viewmodels

import android.app.Application
import androidx.biometric.BiometricManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.raahi.data.security.BiometricAuthUtil
import com.example.raahi.data.security.EncryptedPrefsUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PinSetupUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPinSet: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val enableBiometrics: Boolean = false
)

class PinSetupViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PinSetupUiState())
    val uiState: StateFlow<PinSetupUiState> = _uiState

    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> = _pin

    init {
        val context = getApplication<Application>().applicationContext
        val biometricStatus = BiometricAuthUtil.getBiometricAvailability(context)
        val isAvailable = biometricStatus == BiometricManager.BIOMETRIC_SUCCESS
        val isEnabled = EncryptedPrefsUtil.isBiometricEnabled()

        _uiState.value = _uiState.value.copy(
            isBiometricAvailable = isAvailable,
            enableBiometrics = isEnabled && isAvailable // Only enable if available
        )
    }

    fun onPinChange(newPin: String) {
        _pin.value = newPin
    }

    fun onEnableBiometricsChange(isEnabled: Boolean) {
        _uiState.value = _uiState.value.copy(enableBiometrics = isEnabled)
    }

    fun savePin(enableBiometrics: Boolean) {
        if (_pin.value.length != 4) {
            _uiState.value = _uiState.value.copy(error = "PIN must be 4 digits.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                EncryptedPrefsUtil.savePin(_pin.value)
                EncryptedPrefsUtil.setBiometricEnabled(enableBiometrics)

                _uiState.value = _uiState.value.copy(isLoading = false, isPinSet = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to save PIN.")
            }
        }
    }
}
