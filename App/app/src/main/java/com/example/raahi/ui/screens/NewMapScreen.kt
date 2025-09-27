package com.example.raahi.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.compose.foundation.Image // Added for Logo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource // Added for Logo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView // Import AndroidView for integrating Android Views
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.raahi.R // Added for R.drawable.app_logo
import com.example.raahi.ui.theme.RaahiTheme
import com.example.raahi.ui.viewmodels.MapViewModel
import com.example.raahi.ui.viewmodels.PlaceInfo
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
// Removed Google Maps imports
// import com.google.android.gms.location.LocationServices
// import com.google.android.gms.maps.CameraUpdateFactory
// import com.google.android.gms.maps.model.BitmapDescriptorFactory
// import com.google.android.gms.maps.model.CameraPosition
// import com.google.android.gms.maps.model.LatLng
// import com.google.maps.android.compose.*
// Removed Google Tasks await import
// import kotlinx.coroutines.tasks.await

// osmdroid imports
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import androidx.preference.PreferenceManager // Added missing import
import kotlinx.coroutines.launch // Added missing import

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun NewMapScreen(
    mapViewModel: MapViewModel = viewModel()
) {
    val uiState by mapViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Initialize osmdroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        // Set user agent for osmdroid
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Location permission state
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Effect to handle location updates using LocationManager
    @SuppressLint("MissingPermission")
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val lastKnownLocation: android.location.Location? = try {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("NewMapScreen", "Error getting last known location", e)
                null
            }

            lastKnownLocation?.let { location ->
                mapViewModel.updateCurrentLocation(location)
            } ?: run {
                Log.w("NewMapScreen", "Last known location not available.")
                // Optionally, request a single fresh location update here if needed
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Raahi App Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 8.dp)
                        )
                        Text("Map", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply padding to the Column
            ) {
                if (!locationPermissionState.status.isGranted) {
                    PermissionRequest(
                        onRequestPermission = { locationPermissionState.launchPermissionRequest() }
                    )
                }

                // OSMDroid MapView
                val map = remember { MapView(context) }

                AndroidView(
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    factory = { ctx ->
                        map.apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            // Add MyLocationOverlay
                            val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                            myLocationOverlay.enableMyLocation()
                            // myLocationOverlay.enableFollowLocation() // Enable if you want the map to follow the user
                            myLocationOverlay.isDrawAccuracyEnabled = true
                            overlays.add(myLocationOverlay)

                            // Add CompassOverlay
                            val compassOverlay = CompassOverlay(ctx, InternalCompassOrientationProvider(ctx), this)
                            compassOverlay.enableCompass()
                            overlays.add(compassOverlay)

                            // Add ScaleBarOverlay
                            val scaleBarOverlay = ScaleBarOverlay(this)
                            scaleBarOverlay.setCentred(true)
                            scaleBarOverlay.setScaleBarOffset(ctx.resources.displayMetrics.widthPixels / 2, 10)
                            overlays.add(scaleBarOverlay)

                            // Add RotationGestureOverlay
                            overlays.add(RotationGestureOverlay(this))
                        }
                    },
                    update = { mapView ->
                        // Clear existing markers (except MyLocationOverlay and other fixed overlays)
                        mapView.overlays.clear()
                        // Re-add fixed overlays (MyLocation, Compass, ScaleBar, Rotation)
                        val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
                        myLocationOverlay.enableMyLocation()
                        // myLocationOverlay.enableFollowLocation()
                        myLocationOverlay.isDrawAccuracyEnabled = true
                        mapView.overlays.add(myLocationOverlay)

                        val compassOverlay = CompassOverlay(context, InternalCompassOrientationProvider(context), mapView)
                        compassOverlay.enableCompass()
                        mapView.overlays.add(compassOverlay)

                        val scaleBarOverlay = ScaleBarOverlay(mapView)
                        scaleBarOverlay.setCentred(true)
                        scaleBarOverlay.setScaleBarOffset(context.resources.displayMetrics.widthPixels / 2, 10)
                        mapView.overlays.add(scaleBarOverlay)

                        mapView.overlays.add(RotationGestureOverlay(mapView))

                        // Update map center and markers
                        uiState.currentLocation?.let { geoPoint ->
                            mapView.controller.setCenter(geoPoint)
                            // Add a marker for current location if not following
                            if (!myLocationOverlay.isFollowLocationEnabled) {
                                val currentLocMarker = Marker(mapView)
                                currentLocMarker.position = geoPoint
                                currentLocMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                currentLocMarker.title = "Your Location"
                                mapView.overlays.add(currentLocMarker)
                            }
                        }

                        // Show nearby monuments
                        uiState.nearbyMonuments.forEach { monument ->
                            val marker = Marker(mapView)
                            marker.position = monument.geoPoint
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = monument.name
                            marker.snippet = monument.address
                            marker.icon = ContextCompat.getDrawable(context, R.drawable.ic_marker_monument) // Custom marker icon
                            mapView.overlays.add(marker)
                        }

                        // Show nearby police stations
                        uiState.nearbyPoliceStations.forEach { station ->
                            val marker = Marker(mapView)
                            marker.position = station.geoPoint
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = station.name
                            marker.snippet = station.address
                            marker.icon = ContextCompat.getDrawable(context, R.drawable.ic_marker_police) // Custom marker icon
                            mapView.overlays.add(marker)
                        }
                        mapView.invalidate()
                    }
                )

                // Legend overlay - Wrapped in Box for align modifier
                Box(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            LegendItem(
                                color = Color(0xFF4285F4),
                                text = "Monuments (${uiState.nearbyMonuments.size})"
                            )
                            LegendItem(
                                color = Color(0xFF0F9D58),
                                text = "Police Stations (${uiState.nearbyPoliceStations.size})"
                            )
                        }
                    }
                }

                // Bottom sheet with places list
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 200.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text(
                            "Nearby Places",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            items(uiState.nearbyMonuments) { monument ->
                                PlaceCard(
                                    place = monument,
                                    onClick = {
                                        scope.launch {
                                            // Animate map to selected place
                                            map.controller.animateTo(monument.geoPoint)
                                            map.controller.setZoom(15.0)
                                        }
                                    }
                                )
                            }
                            items(uiState.nearbyPoliceStations) { station ->
                                PlaceCard(
                                    place = station,
                                    onClick = {
                                        scope.launch {
                                            // Animate map to selected place
                                            map.controller.animateTo(station.geoPoint)
                                            map.controller.setZoom(15.0)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.error != null) {
                // Error Text - Wrapped in Box for align modifier
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Error: ${uiState.error}",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .background(Color.Red)
                            .padding(8.dp)
                            .fillMaxWidth(),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRequest(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Location permission is required to show your current location and nearby places",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Grant Permission")
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun PlaceCard(
    place: PlaceInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            place.address?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            place.rating?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFB800)
                    )
                    Text(
                        text = "%.1f".format(it),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewMapScreenPreview() {
    RaahiTheme {
        NewMapScreen()
    }
}
