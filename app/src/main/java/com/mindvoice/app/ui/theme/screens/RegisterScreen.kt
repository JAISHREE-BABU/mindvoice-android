package com.mindvoice.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mindvoice.app.data.local.TokenManager
import com.mindvoice.app.data.remote.LanguageDto
import com.mindvoice.app.data.remote.RegisterRequest
import com.mindvoice.app.data.remote.RetrofitClient
import com.mindvoice.app.data.remote.friendlyErrorMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var languages by remember { mutableStateOf(listOf<LanguageDto>()) }
    var nativeLanguage by remember { mutableStateOf<LanguageDto?>(null) }
    var targetLanguage by remember { mutableStateOf<LanguageDto?>(null) }
    var nativeExpanded by remember { mutableStateOf(false) }
    var targetExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            languages = RetrofitClient.apiService.getLanguages()
        } catch (e: Exception) {
            errorText = friendlyErrorMessage(e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Create your MindVoice account", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (min 8 characters)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = nativeExpanded,
            onExpandedChange = { nativeExpanded = it }
        ) {
            OutlinedTextField(
                value = nativeLanguage?.let { "${it.flagEmoji ?: ""} ${it.name}" } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Native language") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nativeExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = nativeExpanded, onDismissRequest = { nativeExpanded = false }) {
                languages.forEach { lang ->
                    DropdownMenuItem(
                        text = { Text("${lang.flagEmoji ?: ""} ${lang.name}") },
                        onClick = {
                            nativeLanguage = lang
                            nativeExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = targetExpanded,
            onExpandedChange = { targetExpanded = it }
        ) {
            OutlinedTextField(
                value = targetLanguage?.let { "${it.flagEmoji ?: ""} ${it.name}" } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Language you want to learn") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = targetExpanded, onDismissRequest = { targetExpanded = false }) {
                languages.forEach { lang ->
                    DropdownMenuItem(
                        text = { Text("${lang.flagEmoji ?: ""} ${lang.name}") },
                        onClick = {
                            targetLanguage = lang
                            targetExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (errorText != null) {
            Text(text = errorText ?: "", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val native = nativeLanguage
                val target = targetLanguage
                if (native == null || target == null) {
                    errorText = "Please select both languages"
                    return@Button
                }
                errorText = null
                isLoading = true
                scope.launch {
                    try {
                        val response = RetrofitClient.apiService.register(
                            RegisterRequest(
                                email = email.trim(),
                                password = password,
                                name = name.trim(),
                                nativeLanguageCode = native.code,
                                targetLanguageCode = target.code
                            )
                        )
                        TokenManager.saveToken(context, response.token)
                        isLoading = false
                        onRegisterSuccess()
                    } catch (e: Exception) {
                        isLoading = false
                        errorText = friendlyErrorMessage(e)
                    }
                }
            },
            enabled = !isLoading && name.isNotBlank() && email.isNotBlank() &&
                    password.length >= 8 && nativeLanguage != null && targetLanguage != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Creating account..." else "Create Account")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Log In")
        }
    }
}