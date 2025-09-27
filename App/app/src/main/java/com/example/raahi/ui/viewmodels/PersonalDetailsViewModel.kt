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

// Data class to represent User's Personal Details for the UI
data class UserDetails(
    val profileImageUrl: String = "",
    val name: String = "N/A",
    val age: String = "N/A",
    val gender: String = "N/A",
    val nationality: String = "N/A",
    val contactNumber: String = "N/A",
    val emailId: String = "N/A",
    val passportNo: String = "N/A",
    val visaNumber: String = "N/A",
    val visaTimeline: String = "N/A",
    val idNumber: String = "N/A",
    val emergencyContactName: String = "N/A",
    val emergencyContactNumber: String = "N/A",
    val nfcUrl: String? = null,
    val blockchainTxHash: String? = null,
    // Medical Details
    val bloodGroup: String = "N/A",
    val medicalRecord: String = "N/A",
    val allergies: String = "N/A",
    // Insurance Details
    val insuranceAgencyName: String = "N/A",
    val insuranceId: String = "N/A"
)

data class PersonalDetailsUiState(
    val userDetails: UserDetails? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class PersonalDetailsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PersonalDetailsUiState())
    val uiState: StateFlow<PersonalDetailsUiState> = _uiState

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore
    private val touristCollectionRef = db.collection("tourist")

    init {
        loadPersonalDetails()
    }

    fun loadPersonalDetails() {
        _uiState.value = PersonalDetailsUiState(isLoading = true, error = null)
        viewModelScope.launch {
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                _uiState.value = PersonalDetailsUiState(isLoading = false, error = "User not authenticated.")
                Log.w("PersonalDetailsVM", "Attempted to load details but no user is authenticated.")
                return@launch
            }

            try {
                val documentSnapshot = touristCollectionRef.document(firebaseUser.uid).get().await()
                if (documentSnapshot.exists()) {
                    val userProfile = documentSnapshot.toObject<UserProfile>()
                    if (userProfile != null) {
                        val uiDetails = UserDetails(
                            profileImageUrl = userProfile.photoURL ?: "",
                            name = userProfile.name ?: "N/A",
                            age = userProfile.age ?: "N/A",
                            gender = userProfile.gender ?: "N/A",
                            nationality = userProfile.nationality ?: "N/A",
                            contactNumber = userProfile.phone ?: userProfile.phoneNumber ?: "N/A",
                            emailId = userProfile.email ?: "N/A",
                            passportNo = userProfile.passportNumber ?: "N/A",
                            visaNumber = userProfile.visaNumber ?: "N/A",
                            visaTimeline = userProfile.visaTimeline ?: "N/A",
                            idNumber = userProfile.idNumber ?: "N/A",
                            emergencyContactName = userProfile.emergencyContactName ?: "N/A",
                            emergencyContactNumber = userProfile.emergencyContactNumber ?: "N/A",
                            nfcUrl = userProfile.nfcUrl,
                            blockchainTxHash = userProfile.blockchainTxHash,
                            bloodGroup = userProfile.bloodGroup ?: "N/A",
                            medicalRecord = userProfile.medicalRecord ?: "N/A",
                            allergies = userProfile.allergies ?: "N/A",
                            insuranceAgencyName = userProfile.insuranceAgencyName ?: userProfile.travelInsuranceProvider ?: "N/A",
                            insuranceId = userProfile.insuranceId ?: userProfile.travelInsurancePolicyNumber ?: "N/A"
                        )
                        _uiState.value = PersonalDetailsUiState(userDetails = uiDetails, isLoading = false)
                        Log.d("PersonalDetailsVM", "Successfully loaded tourist profile for UID: ${firebaseUser.uid}")
                    } else {
                        _uiState.value = PersonalDetailsUiState(isLoading = false, error = "Failed to parse tourist profile data.")
                        Log.e("PersonalDetailsVM", "Failed to convert Firestore document to UserProfile for UID: ${firebaseUser.uid}")
                    }
                } else {
                    _uiState.value = PersonalDetailsUiState(isLoading = false, error = "No tourist profile data found. Please complete your profile.")
                    Log.d("PersonalDetailsVM", "No profile document found for UID: ${firebaseUser.uid}")
                }
            } catch (e: Exception) {
                _uiState.value = PersonalDetailsUiState(isLoading = false, error = "Error loading tourist profile: ${e.message}")
                Log.e("PersonalDetailsVM", "Exception while loading profile for UID: ${firebaseUser.uid}", e)
            }
        }
    }
}
