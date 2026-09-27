package com.example.githubclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.githubclient.data.model.PrFile
import com.example.githubclient.ui.components.ErrorState
import com.example.githubclient.ui.components.SectionLoading
import com.example.githubclient.ui.theme.ClosedRed
import com.example.githubclient.ui.theme.OpenGreen

@Composable
fun PrFilesScreen(
    prNumber: Int,
    files: LoadState<List<PrFile>>,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Files changed \u00b7 #$prNumber") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            }
        )
        when (files) {
            is LoadState.Loading -> SectionLoading()
            is LoadState.Error -> ErrorState(files.message, onRetry)
            is LoadState.Success -> {
                if (files.data.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No file changes reported.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    val totalAdditions = files.data.sumOf { it.additions }
                    val totalDeletions = files.data.sumOf { it.deletions }
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            Text(
                                "${files.data.size} file${if (files.data.size == 1) "" else "s"} changed \u00b7 " +
                                    "+$totalAdditions/-$totalDeletions",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(files.data, key = { it.filename }) { file -> FileDiffCard(file) }
                        item { Spacer(Modifier.height(40.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileDiffCard(file: PrFile) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(file.filename, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 2)
                    Text(statusLabel(file.status), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row {
                    Text("+${file.additions}", color = OpenGreen, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Text("-${file.deletions}", color = ClosedRed, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
            if (expanded) {
                Spacer(Modifier.height(10.dp))
                if (file.patch.isNullOrBlank()) {
                    Text(
                        "No preview available for this file (binary, renamed with no changes, or too large).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                            .horizontalScroll(rememberScrollState())
                            .padding(10.dp)
                    ) {
                        file.patch.lines().forEach { line ->
                            val color = when {
                                line.startsWith("+") && !line.startsWith("+++") -> OpenGreen
                                line.startsWith("-") && !line.startsWith("---") -> ClosedRed
                                line.startsWith("@@") -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            Text(
                                line,
                                color = color,
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun statusLabel(status: String) = when (status) {
    "added" -> "Added"
    "removed" -> "Removed"
    "modified" -> "Modified"
    "renamed" -> "Renamed"
    else -> status.replaceFirstChar { it.uppercase() }
}
