package com.example.raahi.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * ViewModel responsible for managing voice assistant functionality
 */
class VoiceAssistantViewModel(application: Application) : AndroidViewModel(application) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null

    // LiveData for speech recognition state
    private val _isListening = MutableLiveData<Boolean>(false)
    val isListening: LiveData<Boolean> = _isListening

    private val _spokenText = MutableLiveData<String>("")
    val spokenText: LiveData<String> = _spokenText

    private val _errorMessage = MutableLiveData<String>("")
    val errorMessage: LiveData<String> = _errorMessage

    // Initialize speech recognition and text-to-speech
    init {
        initializeSpeechRecognizer()
        initializeTextToSpeech()
    }

    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(getApplication())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplication())
            setupRecognitionListener()
        } else {
            _errorMessage.value = "Speech recognition is not available on this device"
        }
    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    _errorMessage.value = "Text-to-speech language not supported"
                }
            } else {
                _errorMessage.value = "Text-to-speech initialization failed"
            }
        }
    }

    private fun setupRecognitionListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
                _errorMessage.value = ""
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
            }

            override fun onError(error: Int) {
                _isListening.value = false
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech input"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error"
                }
                _errorMessage.value = errorMsg
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _spokenText.value = matches[0]
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    /**
     * Start listening for voice input
     */
    fun startListening() {
        _spokenText.value = ""
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplication<Application>().packageName)
        }
        speechRecognizer?.startListening(recognizerIntent)
    }

    /**
     * Stop listening for voice input
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    /**
     * Speak the provided text
     */
    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)

        // Estimate when speech will complete based on text length
        viewModelScope.launch {
            val estimatedDuration = text.length * 50L + 500L // Rough estimate
            delay(estimatedDuration)
            onComplete?.invoke()
        }
    }

    /**
     * Greet the user when voice assistant is first opened
     */
    fun greetUser() {
        speak("Hello! I'm your Raahi voice assistant. How can I help you with transportation today?")
    }

    /**
     * Provide suggestions for voice commands
     */
    fun provideTravelSuggestions() {
        speak("You can say things like 'book a bus', 'find metro routes', or ask for help.")
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
