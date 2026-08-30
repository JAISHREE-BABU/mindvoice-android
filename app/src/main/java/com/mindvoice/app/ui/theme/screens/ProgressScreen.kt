package com.mindvoice.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mindvoice.app.data.local.TokenManager
import com.mindvoice.app.data.remote.RetrofitClient
import com.mindvoice.app.data.remote.UserDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ProgressScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var user by remember { mutableStateOf<UserDto?>(null) }
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
                user = RetrofitClient.apiService.getMe("Bearer $token")
                isLoading = false
            } catch (e: Exception) {
                errorText = "Could not load progress: ${e.message}"
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Your Progress", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorText != null) {
            Text(errorText ?: "", color = MaterialTheme.colorScheme.error)
        } else if (user != null) {
            val xp = user!!.xp
            val level = (xp / 100) + 1
            val xpIntoLevel = xp % 100
            val progressFraction = xpIntoLevel / 100f

            Text("Level $level", style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("$xp total XP", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier.fillMaxWidth().height(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("$xpIntoLevel / 100 XP to next level", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(32.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            user!!.proficiencyLevel?.let {
                Text("Proficiency: $it", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }
            user!!.nativeLanguage?.let {
                Text("Native language: $it", style = MaterialTheme.typography.bodyMedium)
            }
            user!!.targetLanguage?.let {
                Text("Learning: $it", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}