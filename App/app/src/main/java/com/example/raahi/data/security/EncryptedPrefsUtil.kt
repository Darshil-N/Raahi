package com.example.raahi.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object EncryptedPrefsUtil {

    private const val PREF_FILE_NAME = "secure_prefs"
    private const val KEY_USER_PIN = "user_pin"
    private const val KEY_FIRST_TIME_LOGIN = "first_time_login"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        if (!this::sharedPreferences.isInitialized) {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            sharedPreferences = EncryptedSharedPreferences.create(
                context,
                PREF_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    fun savePin(pin: String) {
        sharedPreferences.edit().putString(KEY_USER_PIN, pin).commit()
    }

    fun getPin(): String? {
        return sharedPreferences.getString(KEY_USER_PIN, null)
    }

    fun setFirstTimeLogin(isFirstTime: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_FIRST_TIME_LOGIN, isFirstTime).commit()
    }

    fun isFirstTimeLogin(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_TIME_LOGIN, true)
    }

    fun setBiometricEnabled(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, isEnabled).commit()
    }

    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun clearPin() {
        sharedPreferences.edit().remove(KEY_USER_PIN).commit()
    }
}
