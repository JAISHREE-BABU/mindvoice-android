package com.mindvoice.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mindvoice.app.data.local.TokenManager
import com.mindvoice.app.data.remote.LessonDto
import com.mindvoice.app.data.remote.RetrofitClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun LessonsScreen(onLessonClick: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var lessons by remember { mutableStateOf(listOf<LessonDto>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val token = TokenManager.getToken(context).first()
        if (token == null) {
            errorText = "Not logged in"
            isLoading = false
            return@LaunchedEffect
        }
        scope.launch {
            try {
                lessons = RetrofitClient.apiService.getLessons("Bearer $token", "es")
                isLoading = false
            } catch (e: Exception) {
                errorText = "Could not load lessons: ${e.message}"
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Lessons", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorText != null) {
            Text(errorText ?: "", color = MaterialTheme.colorScheme.error)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(lessons) { lesson ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLessonClick(lesson.id) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(lesson.title, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(lesson.description, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${lesson.difficulty} \u00b7 +${lesson.xpReward} XP", style = MaterialTheme.typography.labelSmall)
                            }
                            if (lesson.completed) {
                                Text("\u2705", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}