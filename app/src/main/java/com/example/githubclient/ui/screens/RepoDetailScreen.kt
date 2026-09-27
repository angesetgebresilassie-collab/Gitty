package com.example.githubclient.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.githubclient.data.model.Issue
import com.example.githubclient.data.model.PullRequest
import com.example.githubclient.ui.components.*

enum class RepoTab { ISSUES, PULL_REQUESTS }

@Composable
fun RepoDetailScreen(
    repoFullName: String,
    selectedTab: RepoTab,
    onTabSelected: (RepoTab) -> Unit,
    issues: LoadState<List<Issue>>,
    pullRequests: LoadState<List<PullRequest>>,
    onBack: () -> Unit,
    onIssueClick: (Issue) -> Unit,
    onPrClick: (PullRequest) -> Unit,
    onRetry: () -> Unit,
    onBrowseFiles: () -> Unit,
    onNewIssue: () -> Unit,
    onNewPr: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(repoFullName, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onBrowseFiles) {
                        Icon(Icons.Filled.Folder, contentDescription = "Browse files")
                    }
                }
            )

            TabRow(selectedTabIndex = if (selectedTab == RepoTab.ISSUES) 0 else 1) {
                Tab(
                    selected = selectedTab == RepoTab.ISSUES,
                    onClick = { onTabSelected(RepoTab.ISSUES) },
                    text = { Text("Issues") }
                )
                Tab(
                    selected = selectedTab == RepoTab.PULL_REQUESTS,
                    onClick = { onTabSelected(RepoTab.PULL_REQUESTS) },
                    text = { Text("Pull Requests") }
                )
            }

            when (selectedTab) {
                RepoTab.ISSUES -> IssuesTab(issues, onIssueClick, onRetry)
                RepoTab.PULL_REQUESTS -> PullRequestsTab(pullRequests, onPrClick, onRetry)
            }
        }

        FloatingActionButton(
            onClick = { if (selectedTab == RepoTab.ISSUES) onNewIssue() else onNewPr() },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = if (selectedTab == RepoTab.ISSUES) "New issue" else "New pull request"
            )
        }
    }
}

@Composable
private fun IssuesTab(
    issues: LoadState<List<Issue>>,
    onIssueClick: (Issue) -> Unit,
    onRetry: () -> Unit
) {
    when (issues) {
        is LoadState.Loading -> SectionLoading()
        is LoadState.Error -> ErrorState(issues.message, onRetry)
        is LoadState.Success -> {
            // Filter out pull requests, since GitHub's API technically returns them
            // in the issues endpoint too.
            val realIssues = issues.data.filter { !it.isPullRequest }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    ExplainerCard(
                        title = "What's an Issue?",
                        body = "An issue is a tracked to-do item for a project — a bug report, a feature request, or a question. " +
                            "Anyone with access can open one, discuss it in the comments, attach labels to categorize it, and " +
                            "close it once it's resolved. Think of it as a ticket in a shared task list for the codebase."
                    )
                }
                if (realIssues.isEmpty()) {
                    item {
                        Text("No open issues here.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                    }
                } else {
                    items(realIssues, key = { it.id }) { issue ->
                        IssueCard(issue, onClick = { onIssueClick(issue) })
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun PullRequestsTab(
    pullRequests: LoadState<List<PullRequest>>,
    onPrClick: (PullRequest) -> Unit,
    onRetry: () -> Unit
) {
    when (pullRequests) {
        is LoadState.Loading -> SectionLoading()
        is LoadState.Error -> ErrorState(pullRequests.message, onRetry)
        is LoadState.Success -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    ExplainerCard(
                        title = "What's a Pull Request?",
                        body = "A pull request (PR) proposes a specific code change — someone made edits on a separate branch " +
                            "and is asking for that branch to be reviewed and merged into the main codebase. Reviewers can " +
                            "comment inline on the changed lines, request edits, and approve it. Once approved, it gets " +
                            "\"merged,\" which folds those changes into the project permanently."
                    )
                }
                if (pullRequests.data.isEmpty()) {
                    item {
                        Text("No open pull requests here.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                    }
                } else {
                    items(pullRequests.data, key = { it.id }) { pr ->
                        PrCard(pr, onClick = { onPrClick(pr) })
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun IssueCard(issue: Issue, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                StatusPill(if (issue.state == "open") ItemStatus.OPEN else ItemStatus.CLOSED)
                Spacer(Modifier.width(8.dp))
                Text(
                    "#${issue.number} ${issue.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (issue.state == "closed") TextDecoration.None else TextDecoration.None
                )
            }
            if (issue.labels.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    issue.labels.take(4).forEach { LabelPill(it.name, it.color) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(issue.user.avatarUrl, size = 18)
                Spacer(Modifier.width(6.dp))
                Text("opened by ${issue.user.login}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (issue.comments > 0) {
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Filled.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(3.dp))
                    Text("${issue.comments}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PrCard(pr: PullRequest, onClick: () -> Unit) {
    val status = when {
        pr.merged -> ItemStatus.MERGED
        pr.state == "closed" -> ItemStatus.CLOSED
        pr.draft -> ItemStatus.DRAFT
        else -> ItemStatus.OPEN
    }
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                StatusPill(status)
                Spacer(Modifier.width(8.dp))
                Text("#${pr.number} ${pr.title}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${pr.head.ref} → ${pr.base.ref}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(pr.user.avatarUrl, size = 18)
                Spacer(Modifier.width(6.dp))
                Text("opened by ${pr.user.login}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
