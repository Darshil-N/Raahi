package com.example.raahi.ui.screens

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.raahi.R
import com.example.raahi.NavRoutes
import com.example.raahi.data.security.BiometricAuthUtil
import com.example.raahi.data.security.EncryptedPrefsUtil
import com.example.raahi.ui.theme.RaahiTheme
import com.example.raahi.ui.viewmodels.LoginViewModel
import androidx.core.content.ContextCompat
import androidx.biometric.BiometricManager

// Helper function to find the FragmentActivity from the context
private fun Context.findActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController? = null,
    loginViewModel: LoginViewModel = viewModel()
) {
    val uiState by loginViewModel.uiState.collectAsState()
    val username by loginViewModel.username.collectAsState()
    val password by loginViewModel.password.collectAsState()
    val pin by loginViewModel.pin.collectAsState()
    val context = LocalContext.current
    var showPinLogin by remember { mutableStateOf(false) }
    var biometricError by remember { mutableStateOf("") }

    val activity = context.findActivity()

    // Check if biometrics are both available and enabled by user
    val isBiometricAvailable = BiometricAuthUtil.isBiometricAvailable(context)
    val isBiometricEnabled = EncryptedPrefsUtil.isBiometricEnabled()
    val showBiometricOption = isBiometricAvailable && isBiometricEnabled

    LaunchedEffect(Unit) {
        if (!EncryptedPrefsUtil.isFirstTimeLogin()) {
            showPinLogin = true
        }
    }

    // Navigate to PIN setup or main app screen on successful login
    LaunchedEffect(uiState.isLoginSuccessful, uiState.navigateToPinSetup) {
        if (uiState.navigateToPinSetup) {
            navController?.navigate(NavRoutes.PIN_SETUP_SCREEN)
            loginViewModel.onLoginNavigated() // Reset flags in ViewModel
        } else if (uiState.isLoginSuccessful) {
            navController?.navigate(NavRoutes.APP_MAIN) {
                popUpTo(NavRoutes.LOGIN_SCREEN) { inclusive = true }
            }
            loginViewModel.onLoginNavigated() // Reset flags in ViewModel
        }
    }

    // Function to trigger biometric authentication
    fun triggerBiometricAuth() {
        activity?.let { fragmentActivity ->
            BiometricAuthUtil.showBiometricPrompt(
                activity = fragmentActivity,
                title = "Authenticate to Login",
                subtitle = "Use your biometric to access Raahi",
                description = "Place your finger on the sensor or look at the camera",
                onSuccess = {
                    // On successful biometric authentication, login automatically
                    loginViewModel.onBiometricAuthSuccess()
                    biometricError = ""
                },
                onFailure = { _, errString ->
                    biometricError = "Authentication failed: $errString"
                },
                onError = { errorCode, errString ->
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        biometricError = "Biometric error: $errString"
                    }
                }
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Raahi App Logo",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 32.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Welcome to Raahi",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 24.dp)
                    )

                    if (showPinLogin) {
                        // PIN login UI
                        OutlinedTextField(
                            value = pin,
                            onValueChange = {
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    loginViewModel.onPinChange(it)
                                }
                                // Clear biometric error when user starts typing
                                biometricError = ""
                            },
                            label = { Text("Enter PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showPinLogin = false }) {
                                Text("Use Email Instead")
                            }

                            // Show biometric button only if available and enabled
                            if (showBiometricOption) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    IconButton(
                                        onClick = { triggerBiometricAuth() },
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = "Use Biometric Authentication",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { loginViewModel.loginWithPin() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Login with PIN")
                        }

                        // Quick biometric login button if enabled
                        if (showBiometricOption) {
                            OutlinedButton(
                                onClick = { triggerBiometricAuth() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Biometric Login",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Login with Biometric")
                            }
                        }
                    } else {
                        // Email/Password login UI
                        OutlinedTextField(
                            value = username,
                            onValueChange = { loginViewModel.onUsernameChange(it) },
                            label = { Text("Email") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { loginViewModel.onPasswordChange(it) },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!EncryptedPrefsUtil.isFirstTimeLogin()) {
                            TextButton(
                                onClick = { showPinLogin = true },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Use PIN Instead")
                            }
                        }

                        Button(
                            onClick = { loginViewModel.loginWithPassword() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Login")
                        }
                    }

                    // Show biometric error if any
                    if (biometricError.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = biometricError,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Show login error if any
                    if (!uiState.error.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Biometric status information
                    if (showPinLogin && isBiometricAvailable && !isBiometricEnabled) {
                        Text(
                            text = "ⓘ Biometric login is available but not enabled. You can enable it in PIN setup.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else if (showPinLogin && !isBiometricAvailable) {
                        Text(
                            text = "ⓘ ${BiometricAuthUtil.getBiometricStatusMessage(context)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun LoginScreenPreview() {
    RaahiTheme {
        LoginScreen()
    }
}
