package com.example.raahi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.raahi.NavRoutes
import com.example.raahi.ui.theme.RaahiTheme
import com.example.raahi.ui.viewmodels.BookingViewModel
import com.example.raahi.ui.viewmodels.Monument

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    navController: NavController? = null,
    bookingViewModel: BookingViewModel = viewModel()
) {
    val uiState by bookingViewModel.uiState.collectAsState()

    RaahiTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Monument Bookings", style = MaterialTheme.typography.titleLarge) },
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
                else -> {
                    if (uiState.monuments.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                            Text(
                                "No monuments available for booking at the moment.", 
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.monuments) { monument ->
                                MonumentCard(monument = monument) { monumentId ->
                                    navController?.navigate(NavRoutes.bookingDetails(monumentId))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonumentCard(monument: Monument, onBookNowClicked: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            AsyncImage(
                model = monument.imageUrl,
                contentDescription = "Image of ${monument.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)) // Clip image to match card top corners
                    .background(MaterialTheme.colorScheme.surfaceVariant), // Placeholder bg for image
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = monument.name,
                    style = MaterialTheme.typography.headlineSmall, // Use headlineSmall for card title
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Timings: ${monument.timings}", 
                    style = MaterialTheme.typography.bodyMedium, // Use bodyMedium for details
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Use a more subtle color for details
                )
                Text(
                    "Ticket Price: ${monument.ticketPrice}", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onBookNowClicked(monument.id) },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 8.dp)
                ) {
                    Text("Book Now", style = MaterialTheme.typography.labelMedium) // Use labelMedium for button text
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookingScreenPreview() {
    RaahiTheme {
        BookingScreen(navController = rememberNavController())
    }
}
