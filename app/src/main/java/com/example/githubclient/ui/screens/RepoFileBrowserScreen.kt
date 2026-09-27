package com.example.githubclient.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.githubclient.data.model.RepoContent
import com.example.githubclient.ui.components.ErrorState
import com.example.githubclient.ui.components.SectionLoading

@Composable
fun RepoFileBrowserScreen(
    repoFullName: String,
    currentPath: String,
    contents: LoadState<List<RepoContent>>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenFolder: (String) -> Unit,
    onOpenFile: (RepoContent) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (currentPath.isBlank()) repoFullName else currentPath, maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            }
        )
        when (contents) {
            is LoadState.Loading -> SectionLoading()
            is LoadState.Error -> ErrorState(contents.message, onRetry)
            is LoadState.Success -> {
                if (contents.data.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("This folder is empty.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                        items(contents.data, key = { it.path }) { entry ->
                            FileRow(entry, onClick = {
                                if (entry.isDirectory) onOpenFolder(entry.path) else onOpenFile(entry)
                            })
                        }
                        item { Spacer(Modifier.height(40.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileRow(entry: RepoContent, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (entry.isDirectory) Icons.Filled.Folder else Icons.Filled.Description,
            contentDescription = null,
            tint = if (entry.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(
            entry.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (entry.isDirectory) FontWeight.Medium else FontWeight.Normal
        )
    }
}
