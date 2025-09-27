package com.example.raahi.ui.screens

import android.Manifest // Essential for permission strings
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager // Essential for PERMISSION_GRANTED
import android.graphics.Bitmap
import android.location.LocationManager // Import LocationManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn // Import LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat // Essential for checkSelfPermission
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.raahi.R
import com.example.raahi.ui.theme.RaahiTheme
import com.example.raahi.ui.theme.NegativeRed
import com.example.raahi.ui.viewmodels.SOSViewModel
import com.example.raahi.ui.viewmodels.SafetyScoreHistoryItem
import com.example.raahi.ui.viewmodels.SafetyScoreUiState
import com.example.raahi.ui.viewmodels.SafetyScoreViewModel
import com.example.raahi.ui.viewmodels.VerificationViewModel
import com.example.raahi.utils.QRCodeGenerator
// Removed Google Play Services Location imports
// import com.google.android.gms.location.LocationServices
// import com.google.android.gms.location.Priority
import android.location.Location // For the Location object type itself

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyScoreScreen(
    appNavController: NavController? = null,
    safetyScoreViewModel: SafetyScoreViewModel = viewModel(),
    sosViewModel: SOSViewModel = viewModel(),
    verificationViewModel: VerificationViewModel = viewModel()
) {
    val uiState by safetyScoreViewModel.uiState.collectAsState()
    val sosUiState by sosViewModel.uiState.collectAsState()
    val verificationUiState by verificationViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showInitialSosConfirmDialog by remember { mutableStateOf(false) } // Dialog 1: Confirm SOS
    var showFinalSosSentDialog by remember { mutableStateOf(false) }     // Dialog 2: SOS Sent

    // State for QR code
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            Toast.makeText(context, "Location permission granted. Proceeding with SOS.", Toast.LENGTH_LONG).show()
            // If permissions were granted *after* initial SOS confirmation, now get location
            getAndSendCurrentLocation(context, sosViewModel)
        } else {
            Toast.makeText(context, "Location permission denied. SOS will be sent without precise location.", Toast.LENGTH_LONG).show()
            sosViewModel.triggerSOS(0.0, 0.0) // Send SOS without precise location
        }
    }

    // Effect for showing the FINAL "SOS Sent" dialog
    LaunchedEffect(sosUiState.sosSuccessMessage) {
        // Only show final dialog if there's a success message AND it's not a cancellation confirmation
        if (sosUiState.sosSuccessMessage != null && sosUiState.sosSuccessMessage != "SOS procedure canceled by user.") { 
            showFinalSosSentDialog = true
        }
    }

    LaunchedEffect(sosUiState.sosError) {
        sosUiState.sosError?.let {
            Toast.makeText(context, "SOS Error: $it", Toast.LENGTH_LONG).show()
            sosViewModel.clearSosMessages() // Clear error from ViewModel
        }
    }

    // Effect to generate QR code when blockchainTxHash changes
    LaunchedEffect(verificationUiState.blockchainTxHash) {
        verificationUiState.blockchainTxHash?.let { hash ->
            qrCodeBitmap = QRCodeGenerator.generateQRCode(hash)
        }
    }

    // Dialog 1: Initial SOS Confirmation
    if (showInitialSosConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showInitialSosConfirmDialog = false }, // Dismiss if clicked outside
            title = { Text("Confirm Emergency") },
            text = { Text("Are you sure you want to activate SOS?") },
            confirmButton = {
                Button(
                    onClick = {
                        showInitialSosConfirmDialog = false
                        // Proceed with permission check and sending SOS
                        when {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                                getAndSendCurrentLocation(context, sosViewModel)
                            }
                            else -> {
                                locationPermissionLauncher.launch(
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                )
                            }
                        }
                    }
                ) { Text("Yes, Activate SOS") }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showInitialSosConfirmDialog = false
                        sosViewModel.cancelSosSignal() // Assumes this function exists in SOSViewModel
                        Toast.makeText(context, "SOS procedure canceled.", Toast.LENGTH_SHORT).show() // Feedback for cancellation
                    }
                ) { Text("No, Cancel") }
            }
        )
    }

    // Dialog 2: Final "SOS Sent" Confirmation
    if (showFinalSosSentDialog) {
        AlertDialog(
            onDismissRequest = {
                showFinalSosSentDialog = false
                sosViewModel.clearSosMessages() // Clear success message from ViewModel
            },
            title = { Text("SOS Activated") },
            text = { Text(sosUiState.sosSuccessMessage ?: "Authorities are being alerted. Help is on the way.") },
            confirmButton = {
                Button(onClick = {
                    showFinalSosSentDialog = false
                    sosViewModel.clearSosMessages() // Clear success message from ViewModel
                }) { Text("OK") }
            }
        )
    }

    RaahiTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "Raahi App Logo",
                                modifier = Modifier.size(32.dp).padding(end = 8.dp)
                            )
                            Text("My ID & SOS", style = MaterialTheme.typography.titleLarge)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            when {
                uiState.isLoading || verificationUiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    }
                }
                uiState.error != null || verificationUiState.error != null -> {
                    val error = uiState.error ?: verificationUiState.error
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text(
                            "Error: $error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item { // QR Code Section
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Verification QR Code",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    qrCodeBitmap?.let { bitmap ->
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Verification QR Code",
                                            modifier = Modifier
                                                .size(200.dp)
                                                .padding(16.dp)
                                        )
                                    } ?: CircularProgressIndicator()
                                }
                            }
                        }

                        item { // Safety Score Content
                            SafetyScoreContent(
                                uiState = uiState,
                                sosInProgress = sosUiState.isSendingSos,
                                qrCodeBitmap = verificationUiState.qrCodeBitmap,
                                onSosClicked = {
                                    showInitialSosConfirmDialog = true // Show Dialog 1 first
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Existing dialogs and permission handling
    // ...existing code...
}

@SuppressLint("MissingPermission")
fun getAndSendCurrentLocation(context: Context, sosViewModel: SOSViewModel) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    ) {
        Toast.makeText(context, "Location permission not available for SOS. Sending without precise location.", Toast.LENGTH_LONG).show()
        Log.d("getAndSendCurrentLocation", "Permission check failed unexpectedly before location request.")
        sosViewModel.triggerSOS(0.0, 0.0)
        return
    }

    val lastKnownLocation: android.location.Location? = try {
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    } catch (e: Exception) {
        Log.e("getAndSendCurrentLocation", "Error getting last known location", e)
        null
    }

    if (lastKnownLocation != null) {
        Log.d("getAndSendCurrentLocation", "Location obtained: ${lastKnownLocation.latitude}, ${lastKnownLocation.longitude}")
        sosViewModel.triggerSOS(lastKnownLocation.latitude, lastKnownLocation.longitude)
    } else {
        Log.w("getAndSendCurrentLocation", "Failed to get current location (location is null).")
        Toast.makeText(context, "Could not get current location. Sending SOS without location.", Toast.LENGTH_LONG).show()
        sosViewModel.triggerSOS(0.0, 0.0)
    }
}


@Composable
fun SafetyScoreContent(
    uiState: SafetyScoreUiState,
    sosInProgress: Boolean,
    qrCodeBitmap: Bitmap?,
    modifier: Modifier = Modifier,
    onSosClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SosButton(
            onClick = onSosClicked,
            isLoading = sosInProgress // sosInProgress can also reflect the initial confirmation step if desired
        )
        Spacer(modifier = Modifier.height(24.dp))
        QrNfcIdSection(qrCodeBitmap = qrCodeBitmap, nfcId = "NFC_ID: XYZ123ABC")
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Current Safety Score",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                CircularScoreIndicator(
                    score = uiState.currentScore,
                    maxScore = uiState.maxScore,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.primary,
                    subTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SosButton(
    onClick: () -> Unit,
    isLoading: Boolean
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            disabledContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
                strokeWidth = 3.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Sos,
                    contentDescription = "SOS",
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("SOS", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun QrNfcIdSection(qrCodeBitmap: Bitmap?, nfcId: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("My Digital ID", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        Card(
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(12.dp)),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
                if (qrCodeBitmap != null) {
                    Image(
                        bitmap = qrCodeBitmap.asImageBitmap(),
                        contentDescription = "Verification QR Code",
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text("QR Code not available")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("NFC Tag ID: $nfcId", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CircularScoreIndicator(
    score: Int,
    maxScore: Int = 100,
    primaryColor: Color,
    backgroundColor: Color,
    textColor: Color,
    subTextColor: Color,
    strokeWidth: Dp = 10.dp,
    size: Dp = 150.dp,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(targetValue = score.toFloat(), label = "scoreAnimation")
    val progress = if (maxScore > 0) animatedScore / maxScore else 0f

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360 * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 40.sp),
                color = textColor
            )
            Text(
                text = "/ $maxScore",
                style = MaterialTheme.typography.bodySmall,
                color = subTextColor
            )
        }
    }
}

@Composable
fun ScoreHistoryItemView(item: SafetyScoreHistoryItem) {
    val itemColor = if (item.points >= 0) MaterialTheme.colorScheme.primary else NegativeRed
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = if (item.points >= 0) "Positive point" else "Negative point",
            tint = itemColor,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = (if (item.points > 0) "+" else "") + "${item.points} pts",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = itemColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SafetyScoreScreenPreview() {
    RaahiTheme {
        SafetyScoreScreen(appNavController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun SosButtonPreview() {
    RaahiTheme {
        Column {
            SosButton(onClick = {}, isLoading = false)
            Spacer(Modifier.height(10.dp))
            SosButton(onClick = {}, isLoading = true)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QrNfcIdSectionPreview() {
    RaahiTheme {
        // Updated preview to not require a Bitmap
        QrNfcIdSection(qrCodeBitmap = null, nfcId = "NFC_PREVIEW_123")
    }
}
