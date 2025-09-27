package com.example.raahi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.raahi.ui.theme.RaahiTheme
import com.example.raahi.ui.viewmodels.MedicalDetailsViewModel
import com.example.raahi.ui.viewmodels.MedicalDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalDetailsScreen(
    navController: NavController? = null,
    medicalDetailsViewModel: MedicalDetailsViewModel = viewModel()
) {
    val uiState by medicalDetailsViewModel.uiState.collectAsState()

    RaahiTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Medical Details", style = MaterialTheme.typography.titleLarge) },
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
                uiState.medicalDetails != null -> {
                    MedicalDetailsContent(
                        medicalDetails = uiState.medicalDetails!!,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text(
                            "No medical details available.",
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
fun MedicalDetailsContent(
    medicalDetails: MedicalDetails,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        MedicalSectionCard(title = "Blood Group", value = medicalDetails.bloodGroup)
        MedicalSectionCard(title = "Known Medical Conditions", value = medicalDetails.medicalRecord)
        MedicalSectionCard(title = "Allergies", value = medicalDetails.allergies)

        MedicalSectionCard(title = "Emergency Medical Contact") {
            DetailRow(label = "Name", value = medicalDetails.emergencyContactName)
            DetailRow(label = "Number", value = medicalDetails.emergencyContactNumber)
        }

        MedicalSectionCard(title = "Travel Insurance") {
            DetailRow(label = "Provider", value = medicalDetails.insuranceAgencyName)
            DetailRow(label = "Policy Number", value = medicalDetails.insuranceId)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun MedicalSectionCard(
    title: String,
    value: String? = null,
    items: List<String>? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium, // Adjusted style
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            when {
                value != null -> {
                    Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
                items != null && items.isNotEmpty() -> {
                    items.forEach { item ->
                        Text(
                            text = "• $item", 
                            style = MaterialTheme.typography.bodyLarge, 
                            color = MaterialTheme.colorScheme.onSurface, 
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )
                    }
                }
                items != null && items.isEmpty() -> {
                    Text(
                        text = "None specified", 
                        style = MaterialTheme.typography.bodyLarge, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                }
                content != null -> {
                    content()
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)){
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.4f).padding(end = 8.dp)
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
fun MedicalDetailsScreenPreview() {
    RaahiTheme {
        MedicalDetailsScreen(navController = rememberNavController())
    }
}
