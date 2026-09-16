package com.example.githubclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.githubclient.data.model.GhNotification
import com.example.githubclient.ui.components.ErrorState
import com.example.githubclient.ui.components.ExplainerCard
import com.example.githubclient.ui.components.SectionLoading
import com.example.githubclient.ui.theme.HubBlue

@Composable
fun NotificationsScreen(
    notifications: LoadState<List<GhNotification>>,
    onRetry: () -> Unit,
    onNotificationClick: (GhNotification) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Notifications") })

        when (notifications) {
            is LoadState.Loading -> SectionLoading()
            is LoadState.Error -> ErrorState(notifications.message, onRetry)
            is LoadState.Success -> {
                if (notifications.data.isEmpty()) {
                    Column(
                        Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.NotificationsNone, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("You're all caught up", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            ExplainerCard(
                                title = "What are notifications?",
                                body = "GitHub notifies you when something happens on a repository you're watching or " +
                                    "participating in — a new comment, a mention, a review request, or an update to an " +
                                    "issue or pull request you're involved with."
                            )
                        }
                        items(notifications.data, key = { it.id }) { n ->
                            NotificationRow(n, onClick = { onNotificationClick(n) })
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: GhNotification, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.unread) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            val icon = when (notification.subject.type) {
                "PullRequest" -> Icons.Filled.CallMerge
                "Issue" -> Icons.Filled.BugReport
                "Release" -> Icons.Filled.NewReleases
                else -> Icons.Filled.NotificationsNone
            }
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(HubBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = HubBlue, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    notification.subject.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (notification.unread) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 2
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    notification.repository.fullName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (notification.unread) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(HubBlue))
            }
        }
    }
}
