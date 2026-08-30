package com.mindvoice.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mindvoice.app.data.local.TokenManager
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToLessons: () -> Unit,
    onNavigateToLeaderboard: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Welcome to MindVoice!", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Practice a conversation with your AI tutor, or log out below.")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateToChat, modifier = Modifier.fillMaxWidth()) {
            Text("Start Chatting")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onNavigateToProgress, modifier = Modifier.fillMaxWidth()) {
            Text("View Progress")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onNavigateToLessons, modifier = Modifier.fillMaxWidth()) {
            Text("Lessons")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onNavigateToLeaderboard, modifier = Modifier.fillMaxWidth()) {
            Text("Leaderboard")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = {
            scope.launch {
                TokenManager.clearToken(context)
                onLogout()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Log Out")
        }
    }
}