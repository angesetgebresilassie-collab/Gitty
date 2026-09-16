package com.example.githubclient.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.jeziellago.compose.markdown.MarkdownText
import com.example.githubclient.data.model.Issue
import com.example.githubclient.data.model.IssueComment
import com.example.githubclient.ui.components.*

@Composable
fun IssueDetailScreen(
    issue: LoadState<Issue>,
    comments: LoadState<List<IssueComment>>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSendComment: (String) -> Unit,
    isSendingComment: Boolean
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (issue is LoadState.Success) "Issue #${issue.data.number}" else "Issue") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            }
        )

        when (issue) {
            is LoadState.Loading -> SectionLoading()
            is LoadState.Error -> ErrorState(issue.message, onRetry)
            is LoadState.Success -> {
                val data = issue.data
                var commentText by remember { mutableStateOf("") }

                Column(Modifier.weight(1f)) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        item {
                            ExplainerCard(
                                title = "What's an Issue?",
                                body = "This is a single tracked problem or request for the project. The description below " +
                                    "explains what's being asked for or reported. People discuss it in the comments beneath, " +
                                    "and it gets \"closed\" once someone fixes it, answers it, or decides not to act on it — " +
                                    "closing doesn't delete it, it just marks the conversation as resolved."
                            )
                        }
                        item {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusPill(if (data.state == "open") ItemStatus.OPEN else ItemStatus.CLOSED)
                                    Spacer(Modifier.width(10.dp))
                                    Text(data.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Avatar(data.user.avatarUrl, size = 22)
                                    Spacer(Modifier.width(8.dp))
                                    Text("${data.user.login} opened this issue", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (data.labels.isNotEmpty()) {
                                    Spacer(Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        data.labels.forEach { LabelPill(it.name, it.color) }
                                    }
                                }
                            }
                        }
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(Modifier.padding(16.dp)) {
                                    if (data.body.isNullOrBlank()) {
                                        Text("No description provided.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    } else {
                                        MarkdownText(markdown = data.body)
                                    }
                                }
                            }
                        }
                        item {
                            Text("Comments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        when (comments) {
                            is LoadState.Loading -> item { SectionLoading() }
                            is LoadState.Error -> item { ErrorState(comments.message, onRetry) }
                            is LoadState.Success -> {
                                if (comments.data.isEmpty()) {
                                    item { Text("No comments yet — be the first to reply.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                } else {
                                    items(comments.data, key = { it.id }) { comment ->
                                        CommentCard(comment)
                                    }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }

                    CommentInputBar(
                        text = commentText,
                        onTextChange = { commentText = it },
                        onSend = {
                            onSendComment(commentText)
                            commentText = ""
                        },
                        isSending = isSendingComment
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentCard(comment: IssueComment) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(comment.user.avatarUrl, size = 20)
                Spacer(Modifier.width(8.dp))
                Text(comment.user.login, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            MarkdownText(markdown = comment.body)
        }
    }
}

@Composable
private fun CommentInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {
    Surface(shadowElevation = 6.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Write a comment...") },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f),
                maxLines = 4
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = onSend,
                enabled = text.isNotBlank() && !isSending
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Send, contentDescription = "Send comment")
                }
            }
        }
    }
}
