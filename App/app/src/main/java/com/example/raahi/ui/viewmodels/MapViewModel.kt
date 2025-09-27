package com.example.raahi.ui.viewmodels

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// Removed Google Maps LatLng import
// import com.google.android.gms.maps.model.LatLng
// Removed Google Places imports
// import com.google.android.libraries.places.api.Places
// import com.google.android.libraries.places.api.model.Place
// import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
// import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
// Removed Google Tasks await import
// import kotlinx.coroutines.tasks.await

// Added osmdroid GeoPoint import
import org.osmdroid.util.GeoPoint

data class MapUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentLocation: GeoPoint? = null, // Changed LatLng to GeoPoint
    val nearbyMonuments: List<PlaceInfo> = emptyList(),
    val nearbyPoliceStations: List<PlaceInfo> = emptyList()
)

data class PlaceInfo(
    val id: String,
    val name: String,
    val geoPoint: GeoPoint, // Changed LatLng to GeoPoint
    val address: String?,
    val rating: Float?,
    val type: PlaceType
)

enum class PlaceType {
    MONUMENT,
    POLICE_STATION
}

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    // Removed placesClient
    // private val placesClient: PlacesClient = Places.createClient(application)

    fun updateCurrentLocation(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude) // Changed LatLng to GeoPoint
        _uiState.value = _uiState.value.copy(currentLocation = geoPoint)
        fetchNearbyPlaces(geoPoint) // Pass GeoPoint
    }

    private fun fetchNearbyPlaces(location: GeoPoint) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // TODO: Implement fetching nearby places using an OpenStreetMap-compatible API (e.g., Nominatim, Overpass API)
            // For now, this function will just simulate loading and then clear it.
            kotlinx.coroutines.delay(1000) // Simulate network request
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                // Clear previous results or set empty lists
                nearbyMonuments = emptyList(),
                nearbyPoliceStations = emptyList()
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
