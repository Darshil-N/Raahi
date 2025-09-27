package com.example.raahi.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raahi.ui.screens.RouteInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LocalTransportUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isBookingConfirmed: Boolean = false,

    // Transport type
    val selectedTransportType: String? = null,

    // Available options
    val availableCities: List<String> = listOf("Delhi", "Mumbai", "Bangalore", "Chennai", "Kolkata"),
    val availablePoints: List<String> = emptyList(),
    val availableRoutes: List<RouteInfo> = emptyList(),

    // Selected values
    val selectedCity: String? = null,
    val selectedPointA: String? = null,
    val selectedPointB: String? = null,
    val ticketCount: Int = 1,
    val selectedDate: String? = null,
    val selectedTime: String? = null,
    val selectedRoute: RouteInfo? = null,

    // Calculated values
    val estimatedPrice: Double = 0.0
)

class LocalTransportViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LocalTransportUiState())
    val uiState: StateFlow<LocalTransportUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Transport type selection
    fun selectTransportType(type: String) {
        _uiState.value = _uiState.value.copy(
            selectedTransportType = type,
            selectedCity = null,
            selectedPointA = null,
            selectedPointB = null,
            availablePoints = emptyList(),
            availableRoutes = emptyList(),
            estimatedPrice = 0.0
        )
    }

    // City selection
    fun selectCity(city: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Mock data for different cities and transport types
                val points = getPointsForCity(city, _uiState.value.selectedTransportType ?: "Bus")

                _uiState.value = _uiState.value.copy(
                    selectedCity = city,
                    availablePoints = points,
                    selectedPointA = null,
                    selectedPointB = null,
                    availableRoutes = emptyList(),
                    isLoading = false
                )
                calculatePrice()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load city points: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun selectPointA(point: String) {
        _uiState.value = _uiState.value.copy(selectedPointA = point)
        if (_uiState.value.selectedPointB != null) {
            loadAvailableRoutes()
        }
        calculatePrice()
    }

    fun selectPointB(point: String) {
        _uiState.value = _uiState.value.copy(selectedPointB = point)
        if (_uiState.value.selectedPointA != null) {
            loadAvailableRoutes()
        }
        calculatePrice()
    }

    fun selectTicketCount(count: Int) {
        _uiState.value = _uiState.value.copy(ticketCount = count)
        calculatePrice()
    }

    fun selectDate(date: String) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadAvailableRoutes()
    }

    fun selectTime(time: String) {
        _uiState.value = _uiState.value.copy(selectedTime = time)
        loadAvailableRoutes()
    }

    fun selectRoute(route: RouteInfo) {
        _uiState.value = _uiState.value.copy(
            selectedRoute = route,
            estimatedPrice = route.price * _uiState.value.ticketCount
        )
    }

    fun swapLocations() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            selectedPointA = currentState.selectedPointB,
            selectedPointB = currentState.selectedPointA
        )
        if (_uiState.value.selectedPointA != null && _uiState.value.selectedPointB != null) {
            loadAvailableRoutes()
        }
        calculatePrice()
    }

    fun bookTransport() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Here you would implement actual booking logic
                // For now, just simulate a successful booking
                kotlinx.coroutines.delay(2000) // Simulate API call

                _uiState.value = _uiState.value.copy(
                    isBookingConfirmed = true,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Booking failed: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun loadAvailableRoutes() {
        viewModelScope.launch {
            val currentState = _uiState.value

            // Only load routes if we have both points selected
            if (currentState.selectedPointA == null || currentState.selectedPointB == null) {
                return@launch
            }

            try {
                _uiState.value = currentState.copy(isLoading = true)

                // In a real app, this would fetch from an API
                // Here we're generating mock data
                val mockRoutes = generateMockRoutes(
                    currentState.selectedTransportType ?: "Bus",
                    currentState.selectedPointA,
                    currentState.selectedPointB,
                    currentState.selectedDate,
                    currentState.selectedTime
                )

                _uiState.value = currentState.copy(
                    availableRoutes = mockRoutes,
                    isLoading = false
                )

                // Auto-select first route if available
                if (mockRoutes.isNotEmpty()) {
                    selectRoute(mockRoutes.first())
                }

            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    error = "Failed to load routes: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun calculatePrice() {
        val currentState = _uiState.value
        if (currentState.selectedRoute != null) {
            _uiState.value = currentState.copy(
                estimatedPrice = currentState.selectedRoute.price * currentState.ticketCount
            )
        }
    }

    private fun getPointsForCity(city: String, transportType: String): List<String> {
        // Mock data for different cities
        return when (city.lowercase()) {
            "delhi" -> listOf("Connaught Place", "India Gate", "Chandni Chowk", "Karol Bagh", "Lajpat Nagar")
            "mumbai" -> listOf("CST", "Bandra", "Andheri", "Juhu", "Colaba")
            "bangalore" -> listOf("MG Road", "Indiranagar", "Koramangala", "Whitefield", "Electronic City")
            "chennai" -> listOf("T. Nagar", "Adyar", "Mylapore", "Anna Nagar", "Velachery")
            "kolkata" -> listOf("Park Street", "Salt Lake", "Howrah", "New Town", "Gariahat")
            else -> emptyList()
        }
    }

    private fun generateMockRoutes(
        transportType: String,
        from: String,
        to: String,
        date: String?,
        time: String?
    ): List<RouteInfo> {
        // Generate some mock routes
        val dateStr = date ?: "Today"
        val baseTime = when (time?.lowercase()) {
            "morning" -> "08:00"
            "afternoon" -> "13:00"
            "evening" -> "18:00"
            else -> "12:00"
        }

        val routes = mutableListOf<RouteInfo>()

        // Add a few routes with different times
        val basePrice = if (transportType == "Bus") 80.0 else 60.0
        val baseDuration = if (transportType == "Bus") "60 min" else "45 min"

        // Add 3-5 routes with slight variations
        for (i in 0 until (3..5).random()) {
            val hourOffset = i * 2
            val departHour = (baseTime.split(":")[0].toInt() + hourOffset) % 24
            val departTime = String.format("%02d:%02d", departHour, (0..59).random())

            val arriveHour = (departHour + baseDuration.split(" ")[0].toInt() / 60) % 24
            val arriveMin = (departTime.split(":")[1].toInt() + baseDuration.split(" ")[0].toInt() % 60) % 60
            val arriveTime = String.format("%02d:%02d", arriveHour, arriveMin)

            val priceVariation = (-10..20).random()
            val finalPrice = basePrice + priceVariation

            routes.add(
                RouteInfo(
                    id = "R${100 + i}",
                    routeName = "$transportType ${100 + i}",
                    transportType = transportType,
                    startPoint = from,
                    endPoint = to,
                    departureTime = departTime,
                    arrivalTime = arriveTime,
                    duration = baseDuration,
                    price = finalPrice,
                    availableSeats = (10..50).random()
                )
            )
        }

        return routes
    }
}
