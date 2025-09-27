package com.example.raahi.ui.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raahi.utils.QRCodeGenerator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class VerificationUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val blockchainTxHash: String? = null,
    val qrCodeBitmap: Bitmap? = null,
    val nfcBandCode: String = "" // Added nfcBandCode
)

class VerificationViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    init {
        fetchVerificationData()
    }

    fun fetchVerificationData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val userId = auth.currentUser?.uid
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not logged in"
                    )
                    return@launch
                }

                val documentSnapshot = firestore
                    .collection("tourist")
                    .document(userId)
                    .get()
                    .await()

                val blockchainTxHash = documentSnapshot.getString("blockchainTxHash")
                val nfcBandCode = documentSnapshot.getString("nfcBandCode") ?: ""

                if (blockchainTxHash != null) {
                    val qrCodeBitmap = QRCodeGenerator.generateQRCode(blockchainTxHash)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        blockchainTxHash = blockchainTxHash,
                        qrCodeBitmap = qrCodeBitmap,
                        nfcBandCode = nfcBandCode
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Verification data not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to fetch verification data"
                )
            }
        }
    }

    fun refreshVerificationData() {
        fetchVerificationData()
    }
}
