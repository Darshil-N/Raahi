package com.example.raahi.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.util.*

class VoiceAssistantService(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isListening = false
    private var isSpeaking = false

    // LiveData for observing voice assistant state
    private val _isListening = MutableLiveData(false)
    val isListeningLive: LiveData<Boolean> = _isListening

    private val _spokenText = MutableLiveData<String>()
    val spokenTextLive: LiveData<String> = _spokenText

    private val _voiceCommand = MutableLiveData<VoiceCommand>()
    val voiceCommandLive: LiveData<VoiceCommand> = _voiceCommand

    private val _errorMessage = MutableLiveData<String>()
    val errorMessageLive: LiveData<String> = _errorMessage

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        initializeTextToSpeech()
        initializeSpeechRecognizer()
    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
                // Set speech rate and pitch for better user experience
                textToSpeech?.setSpeechRate(0.9f)
                textToSpeech?.setPitch(1.0f)
            }
        }
    }

    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(createRecognitionListener())
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
                isListening = true
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
                isListening = false
            }

            override fun onError(error: Int) {
                _isListening.value = false
                isListening = false

                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech input matched"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error occurred"
                }
                _errorMessage.value = errorMessage
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0]
                    _spokenText.value = spokenText
                    processVoiceCommand(spokenText)
                }
                _isListening.value = false
                isListening = false
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun startListening() {
        if (!isListening && !isSpeaking) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
            }
            speechRecognizer?.startListening(intent)
        }
    }

    fun stopListening() {
        if (isListening) {
            speechRecognizer?.stopListening()
            _isListening.value = false
            isListening = false
        }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        textToSpeech?.let { tts ->
            isSpeaking = true
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_id")

            // Monitor speech completion
            serviceScope.launch {
                delay(text.length * 50L + 1000L) // Rough estimation
                isSpeaking = false
                onComplete?.invoke()
            }
        }
    }

    private fun processVoiceCommand(spokenText: String) {
        val command = parseVoiceCommand(spokenText.lowercase())
        _voiceCommand.value = command

        // Provide voice feedback
        when (command.type) {
            CommandType.BOOK_TRANSPORT -> {
                speak("I'll help you book transport. ${command.feedback}")
            }
            CommandType.SELECT_TRANSPORT_TYPE -> {
                speak("Setting transport type to ${command.parameters["transportType"]}.")
            }
            CommandType.SET_DESTINATION -> {
                speak("Setting your destination. ${command.feedback}")
            }
            CommandType.SET_DATE_TIME -> {
                speak("Setting your travel date and time. ${command.feedback}")
            }
            CommandType.GET_ROUTES -> {
                speak("Finding available routes for you.")
            }
            CommandType.HELP -> {
                speak("Here's what I can help you with: book transport, select bus or metro, set destination, choose date and time, or find routes. What would you like to do?")
            }
            CommandType.UNKNOWN -> {
                speak("I'm not sure what you meant. You can say things like 'book a bus', 'find metro routes', or 'help' for more options.")
            }
        }
    }

    private fun parseVoiceCommand(text: String): VoiceCommand {
        return when {
            // Transport booking commands
            text.contains("book") && (text.contains("bus") || text.contains("metro") || text.contains("transport")) -> {
                val transportType = when {
                    text.contains("bus") -> "Bus"
                    text.contains("metro") -> "Metro"
                    else -> "transport"
                }
                VoiceCommand(
                    type = CommandType.BOOK_TRANSPORT,
                    originalText = text,
                    parameters = mapOf("action" to "book", "transportType" to transportType),
                    feedback = "What's your starting point and destination?"
                )
            }

            // Transport type selection
            text.contains("select") || text.contains("choose") && (text.contains("bus") || text.contains("metro")) -> {
                val transportType = if (text.contains("bus")) "Bus" else "Metro"
                VoiceCommand(
                    type = CommandType.SELECT_TRANSPORT_TYPE,
                    originalText = text,
                    parameters = mapOf("transportType" to transportType),
                    feedback = "Transport type selected."
                )
            }

            // Destination setting
            text.contains("go to") || text.contains("destination") || text.contains("from") && text.contains("to") -> {
                VoiceCommand(
                    type = CommandType.SET_DESTINATION,
                    originalText = text,
                    parameters = extractLocationInfo(text),
                    feedback = "Please specify your pickup and drop-off locations clearly."
                )
            }

            // Date and time
            text.contains("time") || text.contains("date") || text.contains("when") ||
            text.contains("tomorrow") || text.contains("today") || text.contains("morning") ||
            text.contains("evening") || text.contains("afternoon") -> {
                VoiceCommand(
                    type = CommandType.SET_DATE_TIME,
                    originalText = text,
                    parameters = extractDateTimeInfo(text),
                    feedback = "When would you like to travel?"
                )
            }

            // Route finding
            text.contains("route") || text.contains("find") || text.contains("available") -> {
                VoiceCommand(
                    type = CommandType.GET_ROUTES,
                    originalText = text,
                    parameters = emptyMap(),
                    feedback = "Searching for available routes."
                )
            }

            // Help command
            text.contains("help") || text.contains("what can you do") || text.contains("commands") -> {
                VoiceCommand(
                    type = CommandType.HELP,
                    originalText = text,
                    parameters = emptyMap(),
                    feedback = ""
                )
            }

            else -> {
                VoiceCommand(
                    type = CommandType.UNKNOWN,
                    originalText = text,
                    parameters = emptyMap(),
                    feedback = "Please try a different command."
                )
            }
        }
    }

    private fun extractLocationInfo(text: String): Map<String, String> {
        val params = mutableMapOf<String, String>()

        // Simple pattern matching for common location phrases
        val fromToPattern = Regex("from\\s+([\\w\\s]+?)\\s+to\\s+([\\w\\s]+)", RegexOption.IGNORE_CASE)
        val goToPattern = Regex("go to\\s+([\\w\\s]+)", RegexOption.IGNORE_CASE)

        fromToPattern.find(text)?.let {
            params["from"] = it.groupValues[1].trim()
            params["to"] = it.groupValues[2].trim()
        } ?: goToPattern.find(text)?.let {
            params["to"] = it.groupValues[1].trim()
        }

        return params
    }

    private fun extractDateTimeInfo(text: String): Map<String, String> {
        val params = mutableMapOf<String, String>()

        when {
            text.contains("today") -> params["date"] = "today"
            text.contains("tomorrow") -> params["date"] = "tomorrow"
            text.contains("morning") -> params["time"] = "morning"
            text.contains("afternoon") -> params["time"] = "afternoon"
            text.contains("evening") -> params["time"] = "evening"
        }

        // Extract time patterns like "at 3 PM", "9:30 AM"
        val timePattern = Regex("(\\d{1,2}):?(\\d{2})?\\s*(am|pm)", RegexOption.IGNORE_CASE)
        timePattern.find(text)?.let {
            params["specificTime"] = it.value
        }

        return params
    }

    fun destroy() {
        serviceScope.cancel()
        speechRecognizer?.destroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        speechRecognizer = null
        textToSpeech = null
    }
}

// Data classes for voice commands
data class VoiceCommand(
    val type: CommandType,
    val originalText: String,
    val parameters: Map<String, String>,
    val feedback: String
)

enum class CommandType {
    BOOK_TRANSPORT,
    SELECT_TRANSPORT_TYPE,
    SET_DESTINATION,
    SET_DATE_TIME,
    GET_ROUTES,
    HELP,
    UNKNOWN
}
