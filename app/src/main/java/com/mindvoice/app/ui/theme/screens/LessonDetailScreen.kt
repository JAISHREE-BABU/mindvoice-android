package com.mindvoice.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mindvoice.app.data.local.TokenManager
import com.mindvoice.app.data.remote.LessonDetailDto
import com.mindvoice.app.data.remote.RetrofitClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun LessonDetailScreen(lessonId: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var lesson by remember { mutableStateOf<LessonDetailDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isCompleting by remember { mutableStateOf(false) }
    var completionMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(lessonId) {
        val token = TokenManager.getToken(context).first()
        if (token == null) {
            errorText = "Not logged in"
            isLoading = false
            return@LaunchedEffect
        }
        scope.launch {
            try {
                lesson = RetrofitClient.apiService.getLessonDetail("Bearer $token", lessonId)
                isLoading = false
            } catch (e: Exception) {
                errorText = "Could not load lesson: ${e.message}"
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) {
            Text("\u2190 Back to Lessons")
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorText != null) {
            Text(errorText ?: "", color = MaterialTheme.colorScheme.error)
        } else if (lesson != null) {
            val currentLesson = lesson!!
            Text(currentLesson.title, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(currentLesson.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentLesson.vocabItems) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(item.phrase, style = MaterialTheme.typography.titleMedium)
                            Text(item.translation, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (completionMessage != null) {
                Text(completionMessage ?: "", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    isCompleting = true
                    scope.launch {
                        try {
                            val token = TokenManager.getToken(context).first()
                            if (token != null) {
                                val response = RetrofitClient.apiService.completeLesson("Bearer $token", lessonId)
                                completionMessage = if (response.alreadyCompleted) {
                                    "Already completed. Total XP: ${response.totalXp}"
                                } else {
                                    "+${response.xpAwarded} XP! Total XP: ${response.totalXp}"
                                }
                                lesson = currentLesson.copy(completed = true)
                            }
                            isCompleting = false
                        } catch (e: Exception) {
                            isCompleting = false
                            completionMessage = "Failed: ${e.message}"
                        }
                    }
                },
                enabled = !isCompleting && !currentLesson.completed,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        currentLesson.completed -> "\u2705 Completed"
                        isCompleting -> "Completing..."
                        else -> "Mark as Complete"
                    }
                )
            }
        }
    }
}