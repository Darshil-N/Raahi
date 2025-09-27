package com.example.raahi.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.raahi.data.security.BiometricAuthUtil
import com.example.raahi.data.security.EncryptedPrefsUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "LoginViewModel"

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccessful: Boolean = false,
    val navigateToPinSetup: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> = _pin

    private val auth: FirebaseAuth = Firebase.auth

    fun onUsernameChange(newUsername: String) {
        _username.value = newUsername
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onPinChange(newPin: String) {
        _pin.value = newPin
    }

    fun loginWithPassword() {
        if (_username.value.isBlank() || _password.value.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email and password cannot be empty.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                auth.signInWithEmailAndPassword(_username.value, _password.value).await()
                Log.d(TAG, "Firebase login successful for user: ${_username.value}")

                if (EncryptedPrefsUtil.isFirstTimeLogin()) {
                    EncryptedPrefsUtil.setFirstTimeLogin(false)
                    _uiState.value = _uiState.value.copy(isLoading = false, navigateToPinSetup = true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, isLoginSuccessful = true)
                }

            } catch (e: Exception) {
                Log.w(TAG, "Firebase login failed", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Login failed. Please try again.")
            }
        }
    }

    fun loginWithPin() {
        if (_pin.value.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "PIN cannot be empty.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val savedPin = EncryptedPrefsUtil.getPin()

            if (savedPin != null && savedPin == _pin.value) {
                _uiState.value = _uiState.value.copy(isLoading = false, isLoginSuccessful = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Invalid PIN.")
            }
        }
    }

    fun loginWithBiometrics(showPrompt: () -> Unit) {
        Log.d(TAG, "loginWithBiometrics called")
        val context = getApplication<Application>().applicationContext
        val biometricStatus = BiometricAuthUtil.getBiometricAvailability(context)
        Log.d(TAG, "Biometric availability status: $biometricStatus")

        if (biometricStatus == BiometricManager.BIOMETRIC_SUCCESS) {
            if (EncryptedPrefsUtil.isBiometricEnabled()) {
                Log.d(TAG, "Biometric is enabled. Calling showPrompt lambda.")
                showPrompt()
            } else {
                Log.d(TAG, "Biometric is not enabled.")
                _uiState.value = _uiState.value.copy(error = "Biometric login not enabled. You can enable it when setting your PIN.")
            }
        } else {
            val errorMessage = when (biometricStatus) {
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No biometric features available on this device."
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric features are currently unavailable."
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No biometrics enrolled. Please add a fingerprint in your device settings."
                else -> "Biometric authentication not available."
            }
            Log.d(TAG, "Biometric not available. Error: $errorMessage")
            _uiState.value = _uiState.value.copy(error = errorMessage)
        }
    }

    fun onBiometricAuthSuccess() {
        Log.d(TAG, "onBiometricAuthSuccess called")
        _uiState.value = _uiState.value.copy(isLoginSuccessful = true)
    }

    fun onBiometricAuthError(errorCode: Int, errString: CharSequence) {
        Log.d(TAG, "onBiometricAuthError called. Code: $errorCode, Message: $errString")
        _uiState.value = _uiState.value.copy(error = errString.toString())
    }

    fun onLoginNavigated() {
        Log.d(TAG, "onLoginNavigated called")
        _uiState.value = _uiState.value.copy(
            isLoginSuccessful = false,
            error = null,
            navigateToPinSetup = false
        )
    }
}
