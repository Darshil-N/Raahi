package com.example.raahi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.raahi.NavRoutes
import com.example.raahi.data.security.BiometricAuthUtil
import com.example.raahi.data.security.EncryptedPrefsUtil
import com.example.raahi.ui.viewmodels.PinSetupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupScreen(
    navController: NavController,
    viewModel: PinSetupViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var enableBiometrics by remember { mutableStateOf(false) }

    // Initialize EncryptedPrefsUtil if needed
    LaunchedEffect(Unit) {
        EncryptedPrefsUtil.init(context)
    }

    // Navigate when PIN is successfully set
    LaunchedEffect(uiState.isPinSet) {
        if (uiState.isPinSet) {
            EncryptedPrefsUtil.setFirstTimeLogin(false)
            navController.navigate(NavRoutes.APP_MAIN) {
                popUpTo(NavRoutes.PIN_SETUP_SCREEN) { inclusive = true }
            }
        }
    }

    // Update error state
    LaunchedEffect(uiState.error) {
        error = uiState.error ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Set up your 4-digit PIN",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                    pin = it
                    viewModel.onPinChange(it)
                    error = ""
                }
            },
            label = { Text("Enter 4-digit PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = confirmPin,
            onValueChange = {
                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                    confirmPin = it
                    error = ""
                }
            },
            label = { Text("Confirm PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Biometric Authentication Option
        if (uiState.isBiometricAvailable) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Biometric Authentication",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Enable Biometric Login",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Use fingerprint or face unlock",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = enableBiometrics,
                        onCheckedChange = {
                            enableBiometrics = it
                            viewModel.onEnableBiometricsChange(it)
                        }
                    )
                }
            }
        }

        if (error.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Button(
            onClick = {
                when {
                    pin.length != 4 -> error = "PIN must be 4 digits"
                    pin != confirmPin -> error = "PINs do not match"
                    else -> {
                        if (enableBiometrics && uiState.isBiometricAvailable) {
                            // Test biometric authentication before saving
                            val activity = context as? FragmentActivity
                            if (activity != null) {
                                BiometricAuthUtil.showBiometricPrompt(
                                    activity = activity,
                                    title = "Verify Your Identity",
                                    subtitle = "Use your biometric to confirm setup",
                                    description = "Authenticate to enable biometric login",
                                    onSuccess = {
                                        viewModel.savePin(enableBiometrics)
                                    },
                                    onFailure = { _, errString ->
                                        error = "Biometric verification failed: $errString"
                                    },
                                    onError = { _, errString ->
                                        error = "Biometric error: $errString"
                                    }
                                )
                            } else {
                                // Fallback: save without biometric test
                                viewModel.savePin(enableBiometrics)
                            }
                        } else {
                            viewModel.savePin(enableBiometrics)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Set PIN")
        }

        // Biometric Status Information
        if (uiState.isBiometricAvailable) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "✓ Biometric authentication available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ⓘ ${BiometricAuthUtil.getBiometricStatusMessage(context)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
