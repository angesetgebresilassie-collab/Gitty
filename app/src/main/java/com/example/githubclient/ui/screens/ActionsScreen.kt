package com.example.githubclient.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.githubclient.data.model.WorkflowRun
import com.example.githubclient.ui.components.CheckStatusIcon
import com.example.githubclient.ui.components.ErrorState
import com.example.githubclient.ui.components.ExplainerCard
import com.example.githubclient.ui.components.SectionLoading

@Composable
fun ActionsScreen(
    repoFullName: String,
    runs: LoadState<List<WorkflowRun>>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenRun: (WorkflowRun) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Actions \u00b7 $repoFullName", maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            }
        )
        when (runs) {
            is LoadState.Loading -> SectionLoading()
            is LoadState.Error -> ErrorState(runs.message, onRetry)
            is LoadState.Success -> {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        ExplainerCard(
                            title = "What's a workflow run?",
                            body = "GitHub Actions runs automated jobs — building, testing, checking your code — every time " +
                                "someone pushes or opens a pull request. Each row below is one of those runs. Tap one to see " +
                                "its full logs on GitHub, including any build artifacts (like an APK) it produced."
                        )
                    }
                    if (runs.data.isEmpty()) {
                        item {
                            Text(
                                "No workflow runs yet. Push a commit to trigger one.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    } else {
                        items(runs.data, key = { it.id }) { run -> WorkflowRunCard(run, onClick = { onOpenRun(run) }) }
                    }
                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }
}

@Composable
private fun WorkflowRunCard(run: WorkflowRun, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CheckStatusIcon(run.status, run.conclusion, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    run.displayTitle ?: run.name ?: "Workflow run #${run.runNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "#${run.runNumber} \u00b7 ${run.headBranch ?: "unknown branch"}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
