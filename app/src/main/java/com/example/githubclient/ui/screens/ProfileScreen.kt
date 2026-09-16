package com.example.githubclient.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.githubclient.data.model.AuthenticatedUser
import com.example.githubclient.ui.components.Avatar
import com.example.githubclient.ui.components.ErrorState
import com.example.githubclient.ui.components.SectionLoading

@Composable
fun ProfileScreen(
    user: LoadState<AuthenticatedUser>,
    onRetry: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Profile") })

        when (user) {
            is LoadState.Loading -> SectionLoading()
            is LoadState.Error -> ErrorState(user.message, onRetry)
            is LoadState.Success -> {
                val data = user.data
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Avatar(data.avatarUrl, size = 88)
                    Spacer(Modifier.height(12.dp))
                    Text(data.name ?: data.login, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("@${data.login}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (!data.bio.isNullOrBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(data.bio, style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(Modifier.height(20.dp))
                    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatBlock("Repos", data.publicRepos.toString())
                            StatBlock("Followers", data.followers.toString())
                            StatBlock("Following", data.following.toString())
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sign out")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
