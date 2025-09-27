package com.example.raahi.ui.screens

/**
 * Data class representing a transport route information
 */
data class RouteInfo(
    val id: String,
    val routeName: String,
    val transportType: String,
    val startPoint: String,
    val endPoint: String,
    val departureTime: String,
    val arrivalTime: String,
    val duration: String,
    val price: Double,
    val availableSeats: Int
)
