package com.example.raahi // Make sure this matches your package name

import android.app.Application
import com.example.raahi.data.security.EncryptedPrefsUtil
import com.google.firebase.FirebaseApp

class RaahiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        EncryptedPrefsUtil.init(this)
    }
}
