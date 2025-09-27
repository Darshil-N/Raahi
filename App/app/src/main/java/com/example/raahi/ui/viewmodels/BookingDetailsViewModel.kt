package com.example.raahi.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class MonumentInfo(
    val id: String,
    val name: String,
    val imageUrl: String,
    val ticketPrice: Double
)

data class BookingDetailsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isBookingConfirmed: Boolean = false,
    val monument: MonumentInfo? = null,
    val ticketCount: Int = 1,
    val mainVisitorName: String = "",
    val mainVisitorPhone: String = "",
    val mainVisitorEmail: String = "",
    val additionalVisitors: List<String> = List(3) { "" },
    val totalAmount: Double = 0.0
) {
    fun isValid(): Boolean { // Changed from 'val isFormValid: Boolean get() {' to 'fun isValid(): Boolean {'
        if (mainVisitorName.isBlank()) return false
        if (mainVisitorPhone.isBlank()) return false
        if (mainVisitorEmail.isBlank()) return false

        // Validate additional visitors only if ticketCount > 1
        for (i in 0 until (ticketCount - 1)) {
            // Check if the additional visitor slot exists and is blank
            if (additionalVisitors.getOrNull(i)?.isNotBlank() == false) {
                return false
            }
        }
        return true
    }
}

class BookingDetailsViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookingDetailsUiState())
    val uiState: StateFlow<BookingDetailsUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        val monumentId = savedStateHandle.get<String>("monumentId")
        if (monumentId == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Monument ID not provided."
            )
        } else {
            fetchMonumentDetails(monumentId)
        }
    }

    private fun fetchMonumentDetails(monumentId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val document = firestore.collection("monuments")
                    .document(monumentId)
                    .get()
                    .await()

                val monument = MonumentInfo(
                    id = document.id,
                    name = document.getString("name") ?: "",
                    imageUrl = document.getString("imageUrl") ?: "",
                    ticketPrice = document.getDouble("ticketPrice") ?: 0.0
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    monument = monument,
                    totalAmount = monument.ticketPrice
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun updateTicketCount(count: Int) {
        if (count in 1..4) {
            _uiState.value = _uiState.value.copy(
                ticketCount = count,
                totalAmount = _uiState.value.monument?.ticketPrice?.times(count) ?: 0.0
            )
        }
    }

    fun updateMainVisitorName(name: String) {
        _uiState.value = _uiState.value.copy(mainVisitorName = name)
    }

    fun updateMainVisitorPhone(phone: String) {
        _uiState.value = _uiState.value.copy(mainVisitorPhone = phone)
    }

    fun updateMainVisitorEmail(email: String) {
        _uiState.value = _uiState.value.copy(mainVisitorEmail = email)
    }

    fun updateAdditionalVisitor(index: Int, name: String) {
        val currentVisitors = _uiState.value.additionalVisitors.toMutableList()
        if (index < currentVisitors.size) {
            currentVisitors[index] = name
            _uiState.value = _uiState.value.copy(additionalVisitors = currentVisitors)
        }
    }

    fun confirmBooking() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
                val state = _uiState.value
                val monument = state.monument ?: throw Exception("Monument details not found")

                // Create booking document in Firestore
                val booking = hashMapOf(
                    "userId" to userId,
                    "monumentId" to monument.id,
                    "monumentName" to monument.name,
                    "ticketCount" to state.ticketCount,
                    "totalAmount" to state.totalAmount,
                    "mainVisitor" to hashMapOf(
                        "name" to state.mainVisitorName,
                        "phone" to state.mainVisitorPhone,
                        "email" to state.mainVisitorEmail
                    ),
                    "additionalVisitors" to state.additionalVisitors.take(state.ticketCount - 1),
                    "bookingDate" to System.currentTimeMillis(),
                    "status" to "CONFIRMED"
                )

                firestore.collection("bookings")
                    .add(booking)
                    .await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isBookingConfirmed = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
