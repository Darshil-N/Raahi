package com.example.raahi

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.example.raahi.ui.screens.*
import com.example.raahi.ui.theme.RaahiTheme

@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RaahiTheme {
                val locationPermissionsState = rememberMultiplePermissionsState(
                    permissions = listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )

                // Add notification permission for Android 13+
                val notificationPermissionsState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    rememberMultiplePermissionsState(
                        permissions = listOf(Manifest.permission.POST_NOTIFICATIONS)
                    )
                } else {
                    null
                }

                // Voice assistant permissions
                val voicePermissionsState = rememberMultiplePermissionsState(
                    permissions = listOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS
                    )
                )

                LaunchedEffect(Unit) {
                    // Request location permissions
                    if (!locationPermissionsState.allPermissionsGranted && !locationPermissionsState.shouldShowRationale) {
                        locationPermissionsState.launchMultiplePermissionRequest()
                    }

                    // Request notification permissions for Android 13+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionsState?.let { permissionState ->
                            if (!permissionState.allPermissionsGranted && !permissionState.shouldShowRationale) {
                                permissionState.launchMultiplePermissionRequest()
                            }
                        }
                    }

                    // Request voice assistant permissions
                    if (!voicePermissionsState.allPermissionsGranted && !voicePermissionsState.shouldShowRationale) {
                        voicePermissionsState.launchMultiplePermissionRequest()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController, 
        startDestination = NavRoutes.LOGIN_SCREEN,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable(NavRoutes.LOGIN_SCREEN) {
            LoginScreen(navController = navController)
        }
        composable(NavRoutes.PIN_SETUP_SCREEN) {
            PinSetupScreen(navController = navController)
        }
        composable(NavRoutes.APP_MAIN) {
            MainScreen(appNavController = navController)
        }
        composable(NavRoutes.BOOKING_SCREEN) {
            BookingScreen(navController = navController)
        }
        composable(
            route = NavRoutes.BOOKING_DETAILS_SCREEN,
            arguments = listOf(navArgument("monumentId") { type = NavType.StringType })
        ) {
            BookingDetailsScreen(navController = navController)
        }
        composable(NavRoutes.LOCAL_TRANSPORT_SCREEN) {
            LocalTransportScreen(navController = navController)
        }
        composable(NavRoutes.PERSONAL_DETAILS_SCREEN) {
            PersonalDetailsScreen(navController = navController)
        }
        composable(NavRoutes.MEDICAL_DETAILS_SCREEN) {
            MedicalDetailsScreen(navController = navController)
        }
        composable(NavRoutes.SAFETY_SCORE_SCREEN) {
            SafetyScoreScreen(appNavController = navController) 
        }
        composable(NavRoutes.EMERGENCY_SCREEN) {
            EmergencyScreen()
        }
        composable(NavRoutes.SCORE_HISTORY_SCREEN) {
            ScoreHistoryScreen(navController = navController)
        }
        composable(NavRoutes.VERIFICATION_SCREEN) {
            VerificationScreen(appNavController = navController)
        }
        composable(NavRoutes.NEW_MAP_SCREEN) { 
            NewMapScreen() 
        }
    }
}
