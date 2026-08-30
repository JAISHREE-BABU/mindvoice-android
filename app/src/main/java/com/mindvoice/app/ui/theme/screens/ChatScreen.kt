package com.mindvoice.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mindvoice.app.data.local.TokenManager
import com.mindvoice.app.data.local.VoiceRecorder
import com.mindvoice.app.data.remote.CorrectionDto
import com.mindvoice.app.data.remote.MessageDto
import com.mindvoice.app.data.remote.RetrofitClient
import com.mindvoice.app.data.remote.SendMessageRequest
import com.mindvoice.app.data.remote.StartConversationRequest
import com.mindvoice.app.data.remote.friendlyErrorMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.util.Locale

private fun localeForLanguageCode(code: String): Locale {
    return when (code.trim().lowercase()) {
        "es" -> Locale("es", "ES")
        "fr" -> Locale("fr", "FR")
        "de" -> Locale("de", "DE")
        "ja" -> Locale.JAPAN
        "hi" -> Locale("hi", "IN")
        else -> Locale.US
    }
}

@Composable
fun ChatScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val voiceRecorder = remember { VoiceRecorder(context) }

    val textToSpeech = remember {
        arrayOfNulls<android.speech.tts.TextToSpeech>(1)
    }
    DisposableEffect(Unit) {
        val tts = android.speech.tts.TextToSpeech(context) { }
        textToSpeech[0] = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    var token by remember { mutableStateOf<String?>(null) }
    var conversationId by remember { mutableStateOf<String?>(null) }
    var targetLanguageCode by remember { mutableStateOf("es") }
    var topic by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<MessageDto>()) }
    var lastCorrections by remember { mutableStateOf(listOf<CorrectionDto>()) }
    var totalXp by remember { mutableStateOf<Int?>(null) }
    var lastXpAwarded by remember { mutableStateOf<Int?>(null) }
    var inputText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }

    fun speakText(text: String) {
        val tts = textToSpeech[0] ?: return
        tts.language = localeForLanguageCode(targetLanguageCode)
        tts.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceRecorder.startRecording()
            isRecording = true
        } else {
            errorText = "Microphone permission is required to record voice messages"
        }
    }

    LaunchedEffect(Unit) {
        token = TokenManager.getToken(context).first()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        if (conversationId == null) {
            Text("Start a conversation", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = targetLanguageCode,
                onValueChange = { targetLanguageCode = it },
                label = { Text("Language to practice (e.g. es)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Topic (optional, e.g. ordering food)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (errorText != null) {
                Text(errorText ?: "", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(
                onClick = {
                    val currentToken = token
                    if (currentToken == null) {
                        errorText = "Not logged in"
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        try {
                            val conversation = RetrofitClient.apiService.startConversation(
                                "Bearer $currentToken",
                                StartConversationRequest(
                                    topic = topic.ifBlank { null },
                                    targetLanguageCode = targetLanguageCode.trim()
                                )
                            )
                            conversationId = conversation.id
                            isLoading = false
                        } catch (e: Exception) {
                            isLoading = false
                            errorText = friendlyErrorMessage(e)
                        }
                    }
                },
                enabled = !isLoading && targetLanguageCode.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Starting..." else "Start Conversation")
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total XP: ${totalXp ?: 0}", style = MaterialTheme.typography.titleSmall)
                if (lastXpAwarded != null) {
                    Text("+${lastXpAwarded} XP!", color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    val isUser = message.role == "user"
                    Column(
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = if (isUser) "You" else "MindVoice", style = MaterialTheme.typography.labelSmall)
                        if (isUser) {
                            Text(text = message.content)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = message.content, modifier = Modifier.weight(1f, fill = false))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "\uD83D\uDD0A",
                                    modifier = Modifier.clickable { speakText(message.content) }
                                )
                            }
                        }
                    }
                }
            }

            if (lastCorrections.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Corrections:", style = MaterialTheme.typography.titleSmall)
                lastCorrections.forEach { c ->
                    Text("\u2022 \"${c.original}\" \u2192 \"${c.suggestion}\" (${c.explanation})")
                }
            }

            if (errorText != null) {
                Text(errorText ?: "", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Type a message") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val currentToken = token
                        val currentConversationId = conversationId
                        if (currentToken == null || currentConversationId == null) return@Button
                        val textToSend = inputText
                        isLoading = true
                        errorText = null
                        scope.launch {
                            try {
                                messages = messages + MessageDto(
                                    id = "temp-${System.currentTimeMillis()}",
                                    conversationId = currentConversationId,
                                    role = "user",
                                    content = textToSend,
                                    createdAt = ""
                                )
                                inputText = ""
                                val response = RetrofitClient.apiService.sendMessage(
                                    "Bearer $currentToken",
                                    currentConversationId,
                                    SendMessageRequest(content = textToSend)
                                )
                                messages = messages + response.assistantMessage
                                lastCorrections = response.corrections
                                totalXp = response.totalXp
                                lastXpAwarded = response.xpAwarded
                                isLoading = false
                                speakText(response.assistantMessage.content)
                            } catch (e: Exception) {
                                isLoading = false
                                errorText = friendlyErrorMessage(e)
                            }
                        }
                    },
                    enabled = !isLoading && inputText.isNotBlank()
                ) {
                    Text("Send")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (!isRecording) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            voiceRecorder.startRecording()
                            isRecording = true
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    } else {
                        isRecording = false
                        val file = voiceRecorder.stopRecording()
                        val currentToken = token
                        val currentConversationId = conversationId
                        if (file != null && currentToken != null && currentConversationId != null) {
                            isLoading = true
                            errorText = null
                            scope.launch {
                                try {
                                    val requestBody = file.asRequestBody("audio/aac".toMediaType())
                                    val part = MultipartBody.Part.createFormData("audio", file.name, requestBody)
                                    val response = RetrofitClient.apiService.sendVoiceMessage(
                                        "Bearer $currentToken", currentConversationId, part
                                    )
                                    messages = messages + MessageDto(
                                        id = "temp-${System.currentTimeMillis()}",
                                        conversationId = currentConversationId,
                                        role = "user",
                                        content = response.transcript,
                                        createdAt = ""
                                    )
                                    messages = messages + response.assistantMessage
                                    lastCorrections = response.corrections
                                    totalXp = response.totalXp
                                    lastXpAwarded = response.xpAwarded
                                    isLoading = false
                                    speakText(response.assistantMessage.content)
                                } catch (e: Exception) {
                                    isLoading = false
                                    errorText = friendlyErrorMessage(e)
                                }
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isRecording) "\u23F9 Stop && Send" else "\uD83C\uDFA4 Record Voice Message")
            }
        }
    }
}