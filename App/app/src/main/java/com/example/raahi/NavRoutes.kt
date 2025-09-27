package com.example.raahi

object NavRoutes {
    // Authentication
    const val LOGIN_SCREEN = "login_screen"
    const val PIN_SETUP_SCREEN = "pin_setup_screen"

    // Main app container (hosts bottom navigation)
    const val APP_MAIN = "app_main"

    // Bottom Navigation Tabs / Main Screens
    const val SAFETY_SCORE_SCREEN = "safety_score_screen" // Shows QR, NFC, SOS - first tab
    const val NEW_MAP_SCREEN = "new_map_screen"          // Second tab
    const val MORE_SCREEN = "more_screen"                // Third tab

    // Emergency and Safety Features
    const val EMERGENCY_SCREEN = "emergency_screen"     // Emergency location tracking

    // Destinations accessible from MoreScreen
    const val BOOKING_SCREEN = "booking_screen"
    const val BOOKING_DETAILS_SCREEN = "booking_details_screen/{monumentId}"
    const val LOCAL_TRANSPORT_SCREEN = "local_transport_screen"
    const val PERSONAL_DETAILS_SCREEN = "personal_details_screen"
    const val MEDICAL_DETAILS_SCREEN = "medical_details_screen"
    const val VERIFICATION_SCREEN = "verification_screen"
    // const val SETTINGS_SCREEN = "settings_screen" // Removed
    const val SCORE_HISTORY_SCREEN = "score_history_screen" // New screen for score details

    // Helper function for dynamic routes
    fun bookingDetails(monumentId: String) = "booking_details_screen/$monumentId"
}
