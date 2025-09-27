package com.example.raahi.ui.viewmodels

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle // For positive points
import androidx.compose.material.icons.filled.RemoveCircle // For negative points
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Data class for an item in the score history
data class SafetyScoreHistoryItem(
    val description: String,
    val points: Int,
    val icon: ImageVector // Icon to represent positive/negative impact
)

data class SafetyScoreUiState(
    val currentScore: Int = 0,
    val maxScore: Int = 100,
    val scoreHistory: List<SafetyScoreHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SafetyScoreViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SafetyScoreUiState())
    val uiState: StateFlow<SafetyScoreUiState> = _uiState

    init {
        loadSafetyScore()
    }

    private fun loadSafetyScore() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        // Simulate loading data
        val dummyHistory = listOf(
            SafetyScoreHistoryItem("Staying in a certified safe hotel.", 5, Icons.Filled.AddCircle),
            SafetyScoreHistoryItem("Itinerary includes a remote trek.", -10, Icons.Filled.RemoveCircle),
            SafetyScoreHistoryItem("Traveling solo.", -5, Icons.Filled.RemoveCircle),
            SafetyScoreHistoryItem("Opted-in for real-time tracking.", 5, Icons.Filled.AddCircle),
            SafetyScoreHistoryItem("Completed profile verification.", 10, Icons.Filled.AddCircle)
        )
        val currentScore = 85 // Calculate or fetch actual score
        _uiState.value = SafetyScoreUiState(
            currentScore = currentScore,
            scoreHistory = dummyHistory,
            isLoading = false
        )
    }
}
