plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    id("com.google.gms.google-services") // Keep Google Services plugin for Firebase
}

android {
    namespace = "com.example.raahi"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.raahi"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        // Removed Google Maps API key configuration
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompilerExtension.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Add Gson for JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")

    // Add WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Voice recognition dependencies - Using available versions
    // Remove the libraries that aren't available
    // implementation("androidx.speech:speech:1.0.0-alpha01")
    // implementation("androidx.tts:tts:1.0.0-alpha01")

    // Use Android's built-in speech recognition instead
    implementation("com.google.android.gms:play-services-location:21.0.1") // For location services

    // Built-in Android speech recognition APIs don't require additional dependencies
    // They're part of the Android framework

    implementation(libs.androidx.core.ktx)
    implementation(libs.material) 
    implementation(libs.androidx.lifecycle.livedata.ktx) 
    implementation(libs.androidx.lifecycle.viewmodel.ktx) 

    // Jetpack Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)

    // Coil (for image loading in Compose)
    implementation(libs.coil.compose)

    // Removed Google Maps & Places dependencies
    // implementation(libs.google.maps.compose)
    // implementation(libs.google.play.services.maps)
    // implementation("com.google.android.gms:play-services-location:21.3.0") // Keeping for general location if needed
    // implementation("com.google.android.libraries.places:places:3.4.0")

    // OpenStreetMap (osmdroid)
    implementation("org.osmdroid:osmdroid-android:6.1.18") // Latest stable version
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Accompanist Libraries
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.navigation.animation) 

    // EncryptedSharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Biometrics
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")

    // QR Code Generator
    implementation("com.google.zxing:core:3.5.1")

    // Voice Assistant Dependencies
    // These dependencies are causing build errors and aren't needed
    // Android's built-in SpeechRecognizer and TextToSpeech APIs are sufficient
    // implementation("androidx.speech:speech:1.0.0")
    // implementation("com.google.android.gms:play-services-speech:20.0.0")

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core) 
    androidTestImplementation(libs.androidx.compose.ui.test.junit4) 
}
