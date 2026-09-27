package com.example.githubclient.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText
import com.example.githubclient.data.model.PullRequest
import com.example.githubclient.ui.components.*
import com.example.githubclient.ui.theme.ClosedRed
import com.example.githubclient.ui.theme.MergedPurple
import com.example.githubclient.ui.theme.OpenGreen

@Composable
fun PullRequestDetailScreen(
    pr: LoadState<PullRequest>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onMerge: () -> Unit,
    isMerging: Boolean,
    mergeError: String?,
    onViewFiles: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (pr is LoadState.Success) "Pull Request #${pr.data.number}" else "Pull Request") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            }
        )

        when (pr) {
            is LoadState.Loading -> SectionLoading()
            is LoadState.Error -> ErrorState(pr.message, onRetry)
            is LoadState.Success -> {
                val data = pr.data
                val status = when {
                    data.merged -> ItemStatus.MERGED
                    data.state == "closed" -> ItemStatus.CLOSED
                    data.draft -> ItemStatus.DRAFT
                    else -> ItemStatus.OPEN
                }

                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    item {
                        ExplainerCard(
                            title = "What's a Pull Request?",
                            body = "This proposes merging code from one branch into another. \"${data.head.ref}\" is the " +
                                "branch with the proposed changes; \"${data.base.ref}\" is where they'd land. Green additions " +
                                "and red deletions below show the size of the change. Merging is permanent — it applies " +
                                "those changes to the base branch."
                        )
                    }
                    item {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusPill(status)
                                Spacer(Modifier.width(10.dp))
                                Text(data.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Avatar(data.user.avatarUrl, size = 22)
                                Spacer(Modifier.width(8.dp))
                                Text("${data.user.login} wants to merge into ${data.base.ref}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    item {
                        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column {
                                Row(
                                    Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StatColumn(label = "Commits", value = data.commitCount?.toString() ?: "—")
                                    StatColumn(label = "Files changed", value = data.changedFiles?.toString() ?: "—")
                                    DiffStatColumn(additions = data.additions ?: 0, deletions = data.deletions ?: 0)
                                }
                                OutlinedButton(
                                    onClick = onViewFiles,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).padding(bottom = 12.dp)
                                ) {
                                    Text("View file changes")
                                }
                            }
                        }
                    }

                    item {
                        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(1.dp)) {
                            Box(Modifier.padding(16.dp)) {
                                if (data.body.isNullOrBlank()) {
                                    Text("No description provided.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    MarkdownText(markdown = data.body)
                                }
                            }
                        }
                    }

                    if (data.state == "open" && !data.merged) {
                        item {
                            Column {
                                Button(
                                    onClick = onMerge,
                                    enabled = !isMerging && !data.draft,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MergedPurple),
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                ) {
                                    if (isMerging) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                                    } else {
                                        Text(if (data.draft) "Draft — mark ready to merge" else "Merge pull request")
                                    }
                                }
                                if (mergeError != null) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(mergeError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DiffStatColumn(additions: Int, deletions: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = OpenGreen, modifier = Modifier.size(14.dp))
            Text("$additions", color = OpenGreen, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Filled.Remove, contentDescription = null, tint = ClosedRed, modifier = Modifier.size(14.dp))
            Text("$deletions", color = ClosedRed, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Text("Changes", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
