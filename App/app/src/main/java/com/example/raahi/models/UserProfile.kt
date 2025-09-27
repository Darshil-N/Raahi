package com.example.raahi.models

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserProfile(
    val uid: String = "",
    val email: String? = null,
    val name: String? = null,
    val phone: String? = null, // Updated to match Firestore field name
    val emergencyContactName: String? = null,
    val emergencyContactNumber: String? = null,
    val isInDistress: Boolean = false,
    val location: GeoPoint? = null,
    val nfcUrl: String? = "",
    val blockchainTxHash: String? = null,

    // Personal Details fields
    val age: String? = null,
    val gender: String? = null,
    val nationality: String? = null,
    val passportNumber: String? = null,
    val visaNumber: String? = null,
    val visaTimeline: String? = null,
    val idNumber: String? = null,
    val photoURL: String? = null,
    val role: String? = null,
    val state: String? = null,

    // Medical Details fields
    val bloodGroup: String? = null,
    val medicalRecord: String? = null,
    val allergies: String? = null,

    // Insurance fields
    val insuranceAgencyName: String? = null,
    val insuranceId: String? = null,

    // Legacy fields (keeping for backward compatibility)
    val phoneNumber: String? = null, // Deprecated, use 'phone' instead
    val travelInsuranceProvider: String? = null, // Maps to insuranceAgencyName
    val travelInsurancePolicyNumber: String? = null, // Maps to insuranceId

    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val lastUpdatedAt: Date? = null
)
