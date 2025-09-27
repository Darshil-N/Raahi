package com.example.raahi.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Enum to represent the different screens accessible via bottom navigation
enum class BottomNavScreen {
    SAFETY,
    MY_ID,
    MORE
}

data class MainUiState(
    val currentScreen: BottomNavScreen = BottomNavScreen.SAFETY // Default screen
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    fun onBottomNavItemSelected(screen: BottomNavScreen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }
}
