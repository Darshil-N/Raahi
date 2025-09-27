package com.example.raahi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.raahi.ui.viewmodels.LocalTransportViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalTransportScreen(
    navController: NavController
) {
    val viewModel: LocalTransportViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Local Transport Booking", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transport type selection
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Transport Type",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TransportTypeButton(
                            type = "Bus",
                            icon = Icons.Filled.DirectionsBus,
                            isSelected = uiState.selectedTransportType == "Bus",
                            onSelect = { viewModel.selectTransportType("Bus") }
                        )
                        TransportTypeButton(
                            type = "Metro",
                            icon = Icons.Filled.Train,
                            isSelected = uiState.selectedTransportType == "Metro",
                            onSelect = { viewModel.selectTransportType("Metro") }
                        )
                    }
                }
            }

            // Location selection
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // City selection dropdown
                    LocationDropdown(
                        label = "City",
                        selected = uiState.selectedCity,
                        options = uiState.availableCities,
                        onSelect = { viewModel.selectCity(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // From location
                    LocationDropdown(
                        label = "From",
                        selected = uiState.selectedPointA,
                        options = uiState.availablePoints,
                        onSelect = { viewModel.selectPointA(it) }
                    )

                    // Swap button
                    IconButton(
                        onClick = { viewModel.swapLocations() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Filled.SwapVert, "Swap locations")
                    }

                    // To location
                    LocationDropdown(
                        label = "To",
                        selected = uiState.selectedPointB,
                        options = uiState.availablePoints,
                        onSelect = { viewModel.selectPointB(it) }
                    )
                }
            }

            // Date and time selection
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Date & Time",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Date selection
                    DateSelector(
                        selected = uiState.selectedDate,
                        onSelect = { viewModel.selectDate(it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Time selection
                    TimeSelector(
                        selected = uiState.selectedTime,
                        onSelect = { viewModel.selectTime(it) }
                    )
                }
            }

            // Ticket count
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tickets (${uiState.selectedPointA} to ${uiState.selectedPointB})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TicketCounter(
                        count = uiState.ticketCount,
                        onUpdate = { viewModel.selectTicketCount(it) }
                    )
                }
            }

            // Routes
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Available ${uiState.selectedTransportType} Routes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    RouteList(
                        routes = uiState.availableRoutes,
                        onSelect = { viewModel.selectRoute(it) }
                    )
                }
            }

            // Summary and booking button
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Booking Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Transport: ${uiState.selectedTransportType}")
                    Text("From: ${uiState.selectedPointA}")
                    Text("To: ${uiState.selectedPointB}")
                    Text("Date: ${uiState.selectedDate}")
                    Text("Time: ${uiState.selectedTime}")
                    Text("Tickets: ${uiState.ticketCount}")
                    Text("Total Price: ₹${uiState.estimatedPrice}", fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.bookTransport() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Book Now", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

// Helper Composables
@Composable
fun TransportTypeButton(
    type: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .width(100.dp)
            .clickable { onSelect() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = type,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = type,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDropdown(
    label: String,
    selected: String?,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            leadingIcon = {
                Icon(Icons.Filled.LocationOn, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DateSelector(selected: String?, onSelect: (String) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true }
            .padding(8.dp)
    ) {
        Icon(Icons.Filled.CalendarToday, contentDescription = "Date")
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text("Date", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = selected ?: "Select date",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected != null && selected.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }

    // Simple Date picker would go here with showDatePicker state
    // In a real app, you'd use DatePickerDialog from Material3
}

@Composable
fun TimeSelector(selected: String?, onSelect: (String) -> Unit) {
    var showTimePicker by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showTimePicker = true }
            .padding(8.dp)
    ) {
        Icon(Icons.Filled.AccessTime, contentDescription = "Time")
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text("Time", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = selected ?: "Select time",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected != null && selected.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }

    // Simple Time picker would go here with showTimePicker state
    // In a real app, you'd use TimePickerDialog from Material3
}

@Composable
fun TicketCounter(count: Int, onUpdate: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Person, contentDescription = "Passengers")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Number of tickets", style = MaterialTheme.typography.bodyLarge)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (count > 1) onUpdate(count - 1) },
                enabled = count > 1
            ) {
                Text("-", style = MaterialTheme.typography.headlineSmall)
            }

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            IconButton(
                onClick = { onUpdate(count + 1) }
            ) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
fun RouteList(
    routes: List<RouteInfo>,
    onSelect: (RouteInfo) -> Unit
) {
    Column {
        if (routes.isEmpty()) {
            Text(
                "No routes available for the selected criteria",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            routes.forEach { route ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(route) }
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = route.routeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${route.startPoint} → ${route.endPoint}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Depart: ${route.departureTime} • Arrive: ${route.arrivalTime}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${route.price}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${route.availableSeats} seats",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Divider()
            }
        }
    }
}
