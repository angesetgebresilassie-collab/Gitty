package com.example.githubclient.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onTokenSaved: () -> Unit,
    isValidating: Boolean,
    errorMessage: String?,
    onSubmitToken: (String) -> Unit
) {
    var token by remember { mutableStateOf("") }
    var showToken by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Key,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text("Sign in with a token", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                "Paste a GitHub personal access token. It's encrypted and stored only on this device — never sent anywhere except api.github.com.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("Personal access token") },
                placeholder = { Text("ghp_xxxxxxxxxxxxxxxxxxxx") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showToken = !showToken }) {
                        Icon(if (showToken) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "Toggle visibility")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onSubmitToken(token.trim()) },
                enabled = token.isNotBlank() && !isValidating,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isValidating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Continue")
                }
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://github.com/settings/tokens/new?scopes=repo,notifications,read:user&description=Hub%20Android%20App")
                )
                context.startActivity(intent)
            }) {
                Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Create a token on GitHub")
            }

            Spacer(Modifier.height(24.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("What scopes do I need?", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "• repo — read and manage your repositories, issues, and pull requests\n" +
                            "• notifications — see your GitHub notifications\n" +
                            "• read:user — show your profile info",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
