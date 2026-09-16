package com.example.githubclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.githubclient.data.model.AuthenticatedUser
import com.example.githubclient.data.model.Repo
import com.example.githubclient.ui.components.Avatar
import com.example.githubclient.ui.components.ErrorState
import com.example.githubclient.ui.components.SectionLoading

sealed class LoadState<out T> {
    object Loading : LoadState<Nothing>()
    data class Success<T>(val data: T) : LoadState<T>()
    data class Error(val message: String) : LoadState<Nothing>()
}

@Composable
fun ReposScreen(
    user: LoadState<AuthenticatedUser>,
    repos: LoadState<List<Repo>>,
    onRetry: () -> Unit,
    onRepoClick: (Repo) -> Unit
) {
    var query by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        // Profile header
        if (user is LoadState.Success) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(user.data.avatarUrl, size = 44)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(user.data.name ?: user.data.login, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("@${user.data.login} · ${user.data.publicRepos} repos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Filter your repositories") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(12.dp))

        when (repos) {
            is LoadState.Loading -> SectionLoading()
            is LoadState.Error -> ErrorState(repos.message, onRetry)
            is LoadState.Success -> {
                val filtered = repos.data.filter {
                    query.isBlank() || it.name.contains(query, ignoreCase = true)
                }
                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No repositories match \"$query\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filtered, key = { it.id }) { repo ->
                            RepoCard(repo, onClick = { onRepoClick(repo) })
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun RepoCard(repo: Repo, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(repo.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (repo.private) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Filled.Lock, contentDescription = "Private", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (!repo.description.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    repo.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!repo.language.isNullOrBlank()) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(languageColor(repo.language))
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(repo.language, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(14.dp))
                }
                Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(3.dp))
                Text("${repo.stars}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(14.dp))
                Icon(Icons.Filled.CallSplit, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(3.dp))
                Text("${repo.forks}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (repo.openIssuesCount > 0) {
                    Spacer(Modifier.width(14.dp))
                    Text("${repo.openIssuesCount} open issues", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun languageColor(language: String): androidx.compose.ui.graphics.Color {
    val known = mapOf(
        "Kotlin" to 0xFFA97BFF,
        "Java" to 0xFFB07219,
        "Swift" to 0xFFF05138,
        "Python" to 0xFF3572A5,
        "JavaScript" to 0xFFF1E05A,
        "TypeScript" to 0xFF3178C6,
        "Go" to 0xFF00ADD8,
        "Rust" to 0xFFDEA584,
        "C++" to 0xFFF34B7D,
        "C" to 0xFF555555,
        "Ruby" to 0xFF701516,
        "HTML" to 0xFFE34C26,
        "CSS" to 0xFF563D7C
    )
    val hex = known[language] ?: 0xFF9AA0A6
    return androidx.compose.ui.graphics.Color(hex)
}
