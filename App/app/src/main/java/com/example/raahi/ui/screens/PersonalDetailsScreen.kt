package com.example.raahi.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
// Removed isSaving state imports as the button is removed
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage // For loading profile picture
import com.example.raahi.ui.theme.RaahiTheme
import com.example.raahi.ui.viewmodels.PersonalDetailsViewModel
import com.example.raahi.ui.viewmodels.UserDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsScreen(
    navController: NavController? = null,
    personalDetailsViewModel: PersonalDetailsViewModel = viewModel()
) {
    val uiState by personalDetailsViewModel.uiState.collectAsState()

    RaahiTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Personal Details", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = { navController?.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onPrimaryContainer)
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
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                uiState.userDetails != null -> {
                    UserDetailsContent(
                        userDetails = uiState.userDetails!!,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text(
                            "No details available.", 
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserDetailsContent(
    userDetails: UserDetails, 
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = userDetails.profileImageUrl.ifEmpty { null },
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Basic Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Basic Information", 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                DetailItem("Name", userDetails.name)
                DetailItem("Age", userDetails.age)
                DetailItem("Gender", userDetails.gender)
                DetailItem("Nationality", userDetails.nationality)
                DetailItem("Contact Number", userDetails.contactNumber)
                DetailItem("Email ID", userDetails.emailId)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Travel Documents Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Travel Documents",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                DetailItem("Passport No.", userDetails.passportNo)
                DetailItem("Visa Number", userDetails.visaNumber)
                DetailItem("Visa Timeline", userDetails.visaTimeline)
                DetailItem("ID Number", userDetails.idNumber)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Medical Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Medical Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                DetailItem("Blood Group", userDetails.bloodGroup)
                DetailItem("Medical Conditions", userDetails.medicalRecord)
                DetailItem("Allergies", userDetails.allergies)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Emergency Contact Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Emergency Contact", 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                DetailItem("Name", userDetails.emergencyContactName)
                DetailItem("Number", userDetails.emergencyContactNumber)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Insurance Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Insurance Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                DetailItem("Insurance Agency", userDetails.insuranceAgencyName)
                DetailItem("Policy Number", userDetails.insuranceId)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Digital Information Card (if available)
        if (userDetails.nfcUrl != null || userDetails.blockchainTxHash != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Digital Information",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (userDetails.nfcUrl != null) {
                        DetailItem("NFC URL", userDetails.nfcUrl ?: "N/A")
                    }
                    if (userDetails.blockchainTxHash != null) {
                        DetailItem("Blockchain Hash", userDetails.blockchainTxHash ?: "N/A")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                modifier = Modifier.weight(0.4f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge, 
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(0.6f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
    }
}

@Preview(showBackground = true)
@Composable
fun PersonalDetailsScreenPreview() {
    RaahiTheme {
        PersonalDetailsScreen(navController = rememberNavController())
    }
}
