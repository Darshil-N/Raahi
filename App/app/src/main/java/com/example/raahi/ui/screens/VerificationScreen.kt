package com.example.raahi.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.raahi.ui.theme.RaahiTheme
import com.example.raahi.ui.viewmodels.VerificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    appNavController: NavController? = null,
    verificationViewModel: VerificationViewModel = viewModel()
) {
    val uiState by verificationViewModel.uiState.collectAsState()
    val canPopBack = appNavController?.previousBackStackEntry != null

    RaahiTheme {
        Scaffold(
            topBar = {
                if (canPopBack) {
                    TopAppBar(
                        title = { Text("Verify My ID", style = MaterialTheme.typography.titleLarge) },
                        navigationIcon = {
                            IconButton(onClick = { appNavController?.popBackStack() }) {
                                Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                } else {
                     TopAppBar(
                        title = { Text("Verify My ID", style = MaterialTheme.typography.titleLarge) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text(
                            "Error: ${uiState.error}", 
                            color = MaterialTheme.colorScheme.error, 
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    VerificationContent(
                        qrCodeBitmap = uiState.qrCodeBitmap,
                        nfcBandCode = uiState.nfcBandCode,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
fun VerificationContent(
    qrCodeBitmap: Bitmap?,
    nfcBandCode: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp) // Main content padding
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically) // Center content with spacing
    ) {
        // QR Code Card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f) // Card takes 85% of width
                .aspectRatio(1f), // Make card square-ish for QR
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Scan to Verify Identity",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (qrCodeBitmap != null) {
                    Image(
                        bitmap = qrCodeBitmap.asImageBitmap(),
                        contentDescription = "Verification QR Code",
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("QR Code not available")
                    }
                }
            }
        }

        // NFC Band Code Card
        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp), // More padding for this card
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "NFC Band Code",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = nfcBandCode.ifEmpty { "N/A" }, // Show N/A if empty
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 30.sp),
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun VerificationScreenPreview() {
    RaahiTheme {
        VerificationScreen(appNavController = rememberNavController())
    }
}
