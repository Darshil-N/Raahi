package com.example.raahi.ui.screens

import android.annotation.SuppressLint
import android.Manifest
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.example.raahi.NavRoutes
import com.example.raahi.ui.components.VoiceAssistantFloatingButton
import com.example.raahi.ui.components.VoiceAssistantPanel
import com.example.raahi.ui.viewmodels.EmergencyLocationViewModel
import com.example.raahi.ui.viewmodels.MainViewModel
import com.example.raahi.ui.viewmodels.VoiceAssistantViewModel
import com.example.raahi.ui.theme.RaahiTheme

// Define navigation routes for the bottom bar screens
sealed class BottomBarDestination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : BottomBarDestination("home", "Home", Icons.Filled.Home)
    object Map : BottomBarDestination(NavRoutes.NEW_MAP_SCREEN, "Map", Icons.Filled.Map)
    object More : BottomBarDestination(NavRoutes.MORE_SCREEN, "More", Icons.Filled.MoreHoriz)
}

val bottomNavItems = listOf(
    BottomBarDestination.Home,
    BottomBarDestination.Map,
    BottomBarDestination.More
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, ExperimentalPermissionsApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = viewModel(),
    appNavController: NavController? = null
) {
    val bottomNavController = rememberAnimatedNavController()
    val uiState by mainViewModel.uiState.collectAsState()

    // Voice assistant state
    var showVoiceAssistantPanel by remember { mutableStateOf(false) }
    val voiceViewModel: VoiceAssistantViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    val routeToNavigate = screen.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.label,
                                tint = if (currentDestination?.hierarchy?.any { it.route == routeToNavigate } == true)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = { Text(screen.label, style = MaterialTheme.typography.labelMedium) },
                        selected = currentDestination?.hierarchy?.any { it.route == routeToNavigate } == true,
                        onClick = {
                            bottomNavController.navigate(routeToNavigate) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            VoiceAssistantFloatingButton(
                onVoiceAction = { command ->
                    // Process voice command
                    when {
                        command.contains("home", ignoreCase = true) ||
                        command.contains("go home", ignoreCase = true) ||
                        command.contains("home screen", ignoreCase = true) -> {
                            bottomNavController.navigate(BottomBarDestination.Home.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            voiceViewModel.speak("Navigating to home screen")
                        }
                        command.contains("help", ignoreCase = true) -> {
                            showVoiceAssistantPanel = true
                            voiceViewModel.provideTravelSuggestions()
                        }
                        command.contains("book", ignoreCase = true) &&
                            (command.contains("bus", ignoreCase = true) || command.contains("transport", ignoreCase = true)) -> {
                            appNavController?.navigate(NavRoutes.BOOKING_SCREEN)
                            voiceViewModel.speak("Opening booking screen")
                        }
                        command.contains("map", ignoreCase = true) || command.contains("location", ignoreCase = true) -> {
                            bottomNavController.navigate(BottomBarDestination.Map.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            voiceViewModel.speak("Opening map")
                        }
                        command.contains("local transport", ignoreCase = true) -> {
                            appNavController?.navigate(NavRoutes.LOCAL_TRANSPORT_SCREEN) {
                                // Enable going back to the previous screen
                                launchSingleTop = true
                            }
                            voiceViewModel.speak("Opening local transport screen")
                        }
                        command.contains("personal details", ignoreCase = true) ||
                        command.contains("my details", ignoreCase = true) -> {
                            appNavController?.navigate(NavRoutes.PERSONAL_DETAILS_SCREEN) {
                                // Enable going back to the previous screen
                                launchSingleTop = true
                            }
                            voiceViewModel.speak("Opening personal details")
                        }
                        command.contains("medical", ignoreCase = true) ||
                        command.contains("health", ignoreCase = true) -> {
                            appNavController?.navigate(NavRoutes.MEDICAL_DETAILS_SCREEN) {
                                // Enable going back to the previous screen
                                launchSingleTop = true
                            }
                            voiceViewModel.speak("Opening medical details")
                        }
                        command.contains("go back", ignoreCase = true) ||
                        command.contains("back", ignoreCase = true) ||
                        command.contains("previous", ignoreCase = true) -> {
                            // Try app navigation controller first
                            if (appNavController?.previousBackStackEntry != null) {
                                appNavController.popBackStack()
                                voiceViewModel.speak("Going back")
                            }
                            // Then try bottom navigation controller
                            else if (bottomNavController.previousBackStackEntry != null) {
                                bottomNavController.popBackStack()
                                voiceViewModel.speak("Going back")
                            }
                            else {
                                voiceViewModel.speak("Can't go back any further")
                            }
                        }
                        else -> {
                            // Default handling
                            voiceViewModel.speak("I heard you say: $command. How can I help?")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedNavHost(
                navController = bottomNavController,
                startDestination = BottomBarDestination.Home.route,
                modifier = Modifier.padding(paddingValues),
                enterTransition = { fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) },
                exitTransition = { fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) }
            ) {
                composable("home") {
                    EmergencyHomeScreen()
                }
                composable(NavRoutes.NEW_MAP_SCREEN) {
                    NewMapScreen()
                }
                composable(NavRoutes.MORE_SCREEN) {
                    MoreScreen(appNavController = appNavController)
                }
            }

            // Voice assistant panel
            VoiceAssistantPanel(
                isVisible = showVoiceAssistantPanel,
                onDismiss = { showVoiceAssistantPanel = false },
                onVoiceCommand = { command ->
                    // Process voice command from the panel
                    when {
                        command.contains("book", ignoreCase = true) &&
                            (command.contains("bus", ignoreCase = true) || command.contains("transport", ignoreCase = true)) -> {
                            appNavController?.navigate(NavRoutes.BOOKING_SCREEN)
                            showVoiceAssistantPanel = false
                        }
                        command.contains("map", ignoreCase = true) || command.contains("location", ignoreCase = true) -> {
                            bottomNavController.navigate(BottomBarDestination.Map.route)
                            showVoiceAssistantPanel = false
                        }
                        command.contains("close", ignoreCase = true) || command.contains("exit", ignoreCase = true) -> {
                            showVoiceAssistantPanel = false
                        }
                        else -> {
                            voiceViewModel.speak("I heard: $command. Processing your request.")
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // Greet the user with voice assistant when the app starts
    LaunchedEffect(Unit) {
        voiceViewModel.greetUser()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EmergencyHomeScreen(
    emergencyViewModel: EmergencyLocationViewModel = viewModel()
) {
    val uiState by emergencyViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Permission states
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS
        )
    )

    // FIXED: Only request permissions, don't auto-start tracking
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (!permissionsState.allPermissionsGranted) {
            // Only request permissions if not granted
            permissionsState.launchMultiplePermissionRequest()
        }
        // REMOVED automatic tracking start - user must explicitly press SOS button
    }

    // SOS Confirmation Dialog
    if (uiState.showSOSConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                emergencyViewModel.cancelSOS()
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color.Red,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "🚨 Emergency SOS",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to send an Emergency SOS alert?\n\nThis will:\n• Start continuous location tracking\n• Send your location to emergency services\n• Activate geofencing for danger zones\n• Send SMS if network fails",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        emergencyViewModel.confirmSOS()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = "Confirm SOS"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Yes, Send SOS", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        emergencyViewModel.cancelSOS()
                    }
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
            }
        )
    }

    // SOS Success/Error Message Dialog
    uiState.sosConfirmationMessage?.let { message ->
        AlertDialog(
            onDismissRequest = {
                emergencyViewModel.clearSOSMessage()
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (message.contains("success")) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = if (message.contains("success")) "Success" else "Error",
                        tint = if (message.contains("success")) Color.Green else Color.Red,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (message.contains("success")) "SOS Sent" else "SOS Error",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        emergencyViewModel.clearSOSMessage()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // App Title and Description
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Emergency Location Service",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sends your location to police via SMS when network fails",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Permission Status
        if (!permissionsState.allPermissionsGranted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Permissions Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Location and SMS permissions are required for emergency features",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { permissionsState.launchMultiplePermissionRequest() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Grant Permissions", color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Main Emergency Button - Now with confirmation
        if (permissionsState.allPermissionsGranted) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        // Show confirmation dialog instead of directly sending SOS
                        emergencyViewModel.showSOSConfirmation()
                    },
                    modifier = Modifier.size(200.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Emergency,
                            contentDescription = "Emergency Alert",
                            modifier = Modifier.size(56.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "EMERGENCY\nSOS",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Turn Off SOS Button (shown when emergency mode is active)
            if (uiState.isEmergencyMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Emergency Active",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🚨 SOS MODE ACTIVE 🚨",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Emergency distress signal is ON. Tap below to turn off SOS mode.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                emergencyViewModel.turnOffEmergencyState()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Turn Off SOS"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Turn Off SOS Mode", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Status Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tracking Status
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isTracking)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (uiState.isTracking)
                                Icons.Default.GpsFixed
                            else
                                Icons.Default.GpsOff,
                            contentDescription = "GPS Status",
                            tint = if (uiState.isTracking)
                                MaterialTheme.colorScheme.onTertiaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.isTracking) "Tracking" else "Stopped",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Network Status
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isNetworkAvailable)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (uiState.isNetworkAvailable)
                                Icons.Default.Wifi
                            else
                                Icons.Default.WifiOff,
                            contentDescription = "Network Status",
                            tint = if (uiState.isNetworkAvailable)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.isNetworkAvailable) "Online" else "Offline",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Background Location Tracking Toggle
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Background Tracking",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Continuous location updates every 30 seconds",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = uiState.isTracking,
                        onCheckedChange = { isEnabled ->
                            if (isEnabled) {
                                emergencyViewModel.startBackgroundTracking()
                            } else {
                                emergencyViewModel.stopBackgroundTracking()
                            }
                        }
                    )
                }
            }

            // Current Location Info
            uiState.currentLocation?.let { location ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Current Location"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Current Location",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Lat: ${String.format("%.6f", location.latitude)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Lng: ${String.format("%.6f", location.longitude)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Accuracy: ${location.accuracy.toInt()}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    RaahiTheme {
        MainScreen(appNavController = rememberNavController())
    }
}
