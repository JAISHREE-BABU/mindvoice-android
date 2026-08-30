package com.mindvoice.app.ui.screens

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
import com.mindvoice.app.data.remote.LeaderboardEntryDto
import com.mindvoice.app.data.remote.RetrofitClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun LeaderboardScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var entries by remember { mutableStateOf(listOf<LeaderboardEntryDto>()) }
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
                entries = RetrofitClient.apiService.getLeaderboard("Bearer $token")
                isLoading = false
            } catch (e: Exception) {
                errorText = "Could not load leaderboard: ${e.message}"
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Leaderboard", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorText != null) {
            Text(errorText ?: "", color = MaterialTheme.colorScheme.error)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entries) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (entry.isCurrentUser) {
                            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        } else {
                            CardDefaults.cardColors()
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("#${entry.rank}", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        if (entry.isCurrentUser) "${entry.name} (You)" else entry.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    entry.proficiencyLevel?.let {
                                        Text(it, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            Text("${entry.xp} XP", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}