package com.example.raahi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.raahi.ui.theme.RaahiTheme
import com.example.raahi.ui.viewmodels.SafetyScoreHistoryItem // Reusing from SafetyScoreScreen
import com.example.raahi.ui.viewmodels.SafetyScoreUiState // Reusing from SafetyScoreScreen
import com.example.raahi.ui.viewmodels.SafetyScoreViewModel // Reusing from SafetyScoreScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreHistoryScreen(
    navController: NavController? = null,
    safetyScoreViewModel: SafetyScoreViewModel = viewModel() // Reusing the same ViewModel
) {
    val uiState by safetyScoreViewModel.uiState.collectAsState()

    RaahiTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Safety Score History", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        if (navController?.previousBackStackEntry != null) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
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
                else -> {
                    ScoreHistoryContent(uiState = uiState, modifier = Modifier.padding(paddingValues))
                }
            }
        }
    }
}

@Composable
fun ScoreHistoryContent(uiState: SafetyScoreUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Current Score Indicator
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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

        // Score History List
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Score History Log",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
                )
                if (uiState.scoreHistory.isEmpty()) {
                    Text(
                        "No score history available.", 
                        style = MaterialTheme.typography.bodyLarge, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.scoreHistory) { item ->
                            ScoreHistoryItemView(item) // Reusing from SafetyScoreScreen code
                            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ScoreHistoryScreenPreview() {
    RaahiTheme {
        ScoreHistoryScreen(navController = rememberNavController())
    }
}
