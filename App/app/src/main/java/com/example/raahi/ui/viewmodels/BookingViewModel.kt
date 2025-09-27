package com.example.raahi.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Data class to represent a Monument
data class Monument(
    val id: String,
    val name: String,
    val imageUrl: String, // Placeholder image URL
    val timings: String,
    val ticketPrice: String,
    val description: String = "A beautiful and historic monument."
)

data class BookingUiState(
    val monuments: List<Monument> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class BookingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState

    init {
        loadDummyMonuments()
    }

    private fun loadDummyMonuments() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        // Simulate loading
        val dummyMonuments = listOf(
            Monument("1", "Tawang Monastery", "https://picsum.photos/seed/tawang/400/300", "9 AM - 5 PM", "₹50", "Largest monastery in India."),
            Monument("2", "Kaziranga National Park", "https://picsum.photos/seed/kaziranga/400/300", "6 AM - 4 PM (Safari)", "₹200 (Indian)", "Home of the one-horned rhinoceros."),
            Monument("3", "Kamakhya Temple", "https://picsum.photos/seed/kamakhya/400/300", "8 AM - 1 PM, 2:30 PM - 5:30 PM", "Free", "A famous Shakti Peetha."),
            Monument("4", "Nohkalikai Falls", "https://picsum.photos/seed/nohkalikai/400/300", "All day", "₹20 (Entry)", "Tallest plunge waterfall in India."),
            Monument("5", "Unakoti Hills", "https://picsum.photos/seed/unakoti/400/300", "Sunrise to Sunset", "Free", "Ancient Shaivite place of worship with rock carvings.")
        )
        _uiState.value = BookingUiState(monuments = dummyMonuments, isLoading = false)
    }

    fun bookMonument(monumentId: String) {
        // TODO: Implement actual booking logic
        println("Booking attempt for monument ID: $monumentId")
        // For now, this is handled by a Toast in the Composable
    }
}
