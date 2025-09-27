package com.example.raahi.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.raahi.ui.viewmodels.VoiceAssistantViewModel

@Composable
fun VoiceAssistantFloatingButton(
    onVoiceAction: (String) -> Unit,
    modifier: Modifier = Modifier,
    voiceViewModel: VoiceAssistantViewModel = viewModel()
) {
    val isListening by voiceViewModel.isListening.observeAsState(false)
    val spokenText by voiceViewModel.spokenText.observeAsState("")
    val errorMessage by voiceViewModel.errorMessage.observeAsState("")

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Voice feedback card
        AnimatedVisibility(
            visible = isListening || spokenText.isNotEmpty() || errorMessage.isNotEmpty(),
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .widthIn(max = 280.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        errorMessage.isNotEmpty() -> MaterialTheme.colorScheme.errorContainer
                        isListening -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = when {
                        errorMessage.isNotEmpty() -> errorMessage
                        isListening -> "Listening..."
                        spokenText.isNotEmpty() -> "You said: \"$spokenText\""
                        else -> ""
                    },
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = when {
                        errorMessage.isNotEmpty() -> MaterialTheme.colorScheme.onErrorContainer
                        isListening -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        // Main voice button
        FloatingActionButton(
            onClick = {
                if (isListening) {
                    voiceViewModel.stopListening()
                } else {
                    voiceViewModel.startListening()
                }
            },
            containerColor = if (isListening)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primary,
            contentColor = if (isListening)
                MaterialTheme.colorScheme.onError
            else
                MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .size(64.dp)
                .then(
                    if (isListening) {
                        Modifier.background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            CircleShape
                        )
                    } else Modifier
                )
        ) {
            AnimatedContent(
                targetState = isListening,
                transitionSpec = {
                    (fadeIn() togetherWith fadeOut())
                }
            ) { listening ->
                Icon(
                    imageVector = if (listening) Icons.Filled.MicOff else Icons.Filled.Mic,
                    contentDescription = if (listening) "Stop listening" else "Start voice command",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    // Handle voice action results
    LaunchedEffect(spokenText) {
        if (spokenText.isNotEmpty()) {
            onVoiceAction(spokenText)
        }
    }
}

@Composable
fun VoiceAssistantPanel(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onVoiceCommand: (String) -> Unit,
    modifier: Modifier = Modifier,
    voiceViewModel: VoiceAssistantViewModel = viewModel()
) {
    val isListening by voiceViewModel.isListening.observeAsState(false)
    val spokenText by voiceViewModel.spokenText.observeAsState("")

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Voice Assistant",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Voice status indicator
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Filled.Mic else Icons.Filled.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = if (isListening)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isListening)
                        "I'm listening..."
                    else if (spokenText.isNotEmpty())
                        "Processing: \"$spokenText\""
                    else
                        "Tap the microphone and say a command",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Quick commands
                Text(
                    text = "Try saying:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                val quickCommands = listOf(
                    "Book a bus",
                    "Find metro routes",
                    "Go to [destination]",
                    "Set time to morning",
                    "Help"
                )

                quickCommands.forEach { command ->
                    Text(
                        text = "• \"$command\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Start/Stop listening button
                    Button(
                        onClick = {
                            if (isListening) {
                                voiceViewModel.stopListening()
                            } else {
                                voiceViewModel.startListening()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isListening)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Filled.MicOff else Icons.Filled.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isListening) "Stop" else "Listen")
                    }

                    // Close button
                    OutlinedButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Handle voice commands
    LaunchedEffect(spokenText) {
        if (spokenText.isNotEmpty()) {
            onVoiceCommand(spokenText)
        }
    }
}

@Composable
fun VoiceCommandSuggestions(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(suggestions) { suggestion ->
            SuggestionChip(
                onClick = { onSuggestionClick(suggestion) },
                label = {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}
