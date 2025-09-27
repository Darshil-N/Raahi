package com.example.raahi.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raahi.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data class to represent User's Medical Details for the UI
data class MedicalDetails(
    val name: String = "N/A",
    val age: String = "N/A",
    val gender: String = "N/A",
    val bloodGroup: String = "N/A",
    val medicalRecord: String = "N/A", // Known Medical Conditions
    val allergies: String = "N/A",
    val emergencyContactName: String = "N/A",
    val emergencyContactNumber: String = "N/A",
    // Insurance Information
    val insuranceAgencyName: String = "N/A",
    val insuranceId: String = "N/A",
    val profileImageUrl: String = ""
)

data class MedicalDetailsUiState(
    val medicalDetails: MedicalDetails? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class MedicalDetailsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MedicalDetailsUiState())
    val uiState: StateFlow<MedicalDetailsUiState> = _uiState

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore
    private val touristCollectionRef = db.collection("tourist")

    init {
        loadMedicalDetails()
    }

    fun loadMedicalDetails() {
        _uiState.value = MedicalDetailsUiState(isLoading = true, error = null)
        viewModelScope.launch {
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                _uiState.value = MedicalDetailsUiState(isLoading = false, error = "User not authenticated.")
                Log.w("MedicalDetailsVM", "Attempted to load details but no user is authenticated.")
                return@launch
            }

            try {
                val documentSnapshot = touristCollectionRef.document(firebaseUser.uid).get().await()
                if (documentSnapshot.exists()) {
                    val userProfile = documentSnapshot.toObject<UserProfile>()
                    if (userProfile != null) {
                        val medicalDetails = MedicalDetails(
                            name = userProfile.name ?: "N/A",
                            age = userProfile.age ?: "N/A",
                            gender = userProfile.gender ?: "N/A",
                            bloodGroup = userProfile.bloodGroup ?: "N/A",
                            medicalRecord = userProfile.medicalRecord ?: "N/A",
                            allergies = userProfile.allergies ?: "N/A",
                            emergencyContactName = userProfile.emergencyContactName ?: "N/A",
                            emergencyContactNumber = userProfile.emergencyContactNumber ?: "N/A",
                            insuranceAgencyName = userProfile.insuranceAgencyName ?: userProfile.travelInsuranceProvider ?: "N/A",
                            insuranceId = userProfile.insuranceId ?: userProfile.travelInsurancePolicyNumber ?: "N/A",
                            profileImageUrl = userProfile.photoURL ?: ""
                        )
                        _uiState.value = MedicalDetailsUiState(medicalDetails = medicalDetails, isLoading = false)
                        Log.d("MedicalDetailsVM", "Successfully loaded medical details for UID: ${firebaseUser.uid}")
                    } else {
                        _uiState.value = MedicalDetailsUiState(isLoading = false, error = "Failed to parse medical data.")
                        Log.e("MedicalDetailsVM", "Failed to convert Firestore document to UserProfile for UID: ${firebaseUser.uid}")
                    }
                } else {
                    _uiState.value = MedicalDetailsUiState(isLoading = false, error = "No medical data found. Please complete your profile.")
                    Log.d("MedicalDetailsVM", "No profile document found for UID: ${firebaseUser.uid}")
                }
            } catch (e: Exception) {
                _uiState.value = MedicalDetailsUiState(isLoading = false, error = "Error loading medical details: ${e.message}")
                Log.e("MedicalDetailsVM", "Exception while loading medical details for UID: ${firebaseUser.uid}", e)
            }
        }
    }
}
