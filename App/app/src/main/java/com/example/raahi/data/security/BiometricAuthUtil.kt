package com.example.raahi.data.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricAuthUtil {

    fun getBiometricAvailability(context: Context): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        description: String,
        onSuccess: () -> Unit,
        onFailure: (errorCode: Int, errString: CharSequence) -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Don't call error for user cancellation
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        onError(errorCode, errString)
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailure(0, "Authentication failed")
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun isBiometricAvailable(context: Context): Boolean {
        return getBiometricAvailability(context) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun getBiometricStatusMessage(context: Context): String {
        val biometricStatus = getBiometricAvailability(context)
        return when (biometricStatus) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Biometric authentication is available"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No biometric credentials enrolled. Please set up fingerprint or face unlock in device settings."
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No biometric hardware available on this device"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric hardware is currently unavailable"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "Security update required for biometric authentication"
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "Biometric authentication is not supported on this device"
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> "Biometric status unknown"
            else -> "Biometric authentication is not available"
        }
    }
}
