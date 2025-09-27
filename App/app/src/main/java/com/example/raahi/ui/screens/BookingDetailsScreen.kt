package com.example.raahi.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.raahi.ui.theme.RaahiTheme
import com.example.raahi.ui.viewmodels.BookingDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(
    navController: NavController,
    bookingDetailsViewModel: BookingDetailsViewModel = viewModel()
) {
    val uiState by bookingDetailsViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isBookingConfirmed) {
        if (uiState.isBookingConfirmed) {
            Toast.makeText(context, "Booking confirmed!", Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
    }

    RaahiTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Book Your Tickets") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Monument Image
                AsyncImage(
                    model = uiState.monument?.imageUrl,
                    contentDescription = "Monument Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Monument Details
                    Text(
                        text = uiState.monument?.name ?: "Monument",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Price per ticket: ₹${uiState.monument?.ticketPrice ?: 0}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Ticket Quantity Selector
                    TicketCountDropdown(
                        selectedCount = uiState.ticketCount,
                        onCountSelected = { bookingDetailsViewModel.updateTicketCount(it) }
                    )

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    // Visitor Details Form
                    Text(
                        text = "Visitor Details",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Main Visitor
                    OutlinedTextField(
                        value = uiState.mainVisitorName,
                        onValueChange = { bookingDetailsViewModel.updateMainVisitorName(it) },
                        label = { Text("Primary Visitor Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.mainVisitorPhone,
                        onValueChange = { bookingDetailsViewModel.updateMainVisitorPhone(it) },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.mainVisitorEmail,
                        onValueChange = { bookingDetailsViewModel.updateMainVisitorEmail(it) },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    // Additional Visitors
                    if (uiState.ticketCount > 1) {
                        Text(
                            text = "Additional Visitors",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        uiState.additionalVisitors.take(uiState.ticketCount - 1).forEachIndexed { index, visitor ->
                            OutlinedTextField(
                                value = visitor,
                                onValueChange = { bookingDetailsViewModel.updateAdditionalVisitor(index, it) },
                                label = { Text("Visitor ${index + 2} Name") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                singleLine = true
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    // Total Amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Amount",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "₹${uiState.totalAmount}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Book Now Button
                    Button(
                        onClick = { bookingDetailsViewModel.confirmBooking() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        enabled = uiState.isValid()
                    ) {
                        Text("Book Now")
                    }
                }
            }

            // Error Message
            if (uiState.error != null) {
                Toast.makeText(context, uiState.error, Toast.LENGTH_LONG).show()
                bookingDetailsViewModel.clearError()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketCountDropdown(selectedCount: Int, onCountSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val items = (1..4).toList()

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = "$selectedCount Ticket(s)",
            onValueChange = {},
            readOnly = true,
            label = { Text("Number of Tickets") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { count ->
                DropdownMenuItem(
                    text = { Text("$count Ticket(s)") },
                    onClick = {
                        onCountSelected(count)
                        expanded = false
                    }
                )
            }
        }
    }
}
