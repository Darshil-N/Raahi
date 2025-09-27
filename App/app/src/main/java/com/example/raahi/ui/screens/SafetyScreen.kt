package com.example.raahi.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn 
import androidx.compose.material.icons.filled.Place 
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.raahi.ui.theme.RaahiTheme
import com.example.raahi.ui.viewmodels.SafetyViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SafetyScreen(
    // This screen might not need appNavController if it's self-contained or uses its own navigation logic for deeper dives (if any)
    // If it's purely for SOS and location, it might not navigate away using appNavController.
    appNavController: NavController? = null, 
    safetyViewModel: SafetyViewModel = viewModel()
) {
    val uiState by safetyViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )

    // Request permissions when the screen is first composed
    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }
    
    // This screen doesn't have a top app bar in the provided design, 
    // assuming it's a full-screen experience for the map and SOS.
    // If it were part of a scaffold with a top bar, RaahiTheme would be applied there.
    // For this standalone screen content, ensure RaahiTheme wraps its usage if not already covered by a parent Scaffold.

    RaahiTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top 60%: Google Maps View Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f)
                        .background(MaterialTheme.colorScheme.surfaceVariant), // Use theme color
                    contentAlignment = Alignment.Center
                ) {
                    if (locationPermissionsState.allPermissionsGranted) {
                        if (uiState.isMapReady) {
                            Text(
                                "Google Maps View Placeholder\nUser Location: ${uiState.userLocation ?: "Fetching..."}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            // TODO: Implement actual GoogleMap composable here
                            // Placeholder icons with theme colors
                            Column(
                                horizontalAlignment = Alignment.End, 
                                verticalArrangement = Arrangement.Bottom, 
                                modifier = Modifier.fillMaxSize().padding(16.dp)
                            ) {
                                Icon(
                                    Icons.Filled.LocationOn, 
                                    contentDescription = "Nearest Police Station", 
                                    tint = MaterialTheme.colorScheme.secondary, // Use theme accent
                                    modifier = Modifier.size(36.dp).padding(4.dp)
                                )
                                Icon(
                                    Icons.Filled.Place, 
                                    contentDescription = "Nearest Monument", 
                                    tint = MaterialTheme.colorScheme.tertiary, // Use another theme accent
                                    modifier = Modifier.size(36.dp).padding(4.dp)
                                )
                            }
                        } else {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                        }
                    } else {
                        Button(
                            onClick = { locationPermissionsState.launchMultiplePermissionRequest() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 8.dp)
                        ) {
                            Text("Request Location Permissions", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                // Bottom 40%: Panic Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            safetyViewModel.triggerPanicAlert()
                            Toast.makeText(context, "Panic Alert Triggered! Sending location to authorities.", Toast.LENGTH_LONG).show()
                        },
                        shape = CircleShape,
                        modifier = Modifier.size(180.dp), // Slightly larger SOS button
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error, // Use theme error color for SOS
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 8.dp)
                    ) {
                        Text(
                            "SOS",
                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
        }
    }

    // Toast messages for feedback from ViewModel
    if (uiState.panicAlertSent) {
        // Toast.makeText(context, "Panic alert processing...", Toast.LENGTH_SHORT).show() // Already shown onClick
    }
    uiState.error?.let {
        Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
        // Consider a Snackbar for more persistent errors if appropriate
    }
}

@Preview(showBackground = true)
@Composable
fun SafetyScreenPreview() {
    RaahiTheme {
        SafetyScreen(appNavController = rememberNavController())
    }
}
