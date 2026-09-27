package com.example.githubclient.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.githubclient.data.model.RepoContent
import com.example.githubclient.data.model.decodedText
import com.example.githubclient.ui.components.ErrorState
import com.example.githubclient.ui.components.SectionLoading

@Composable
fun FileViewerScreen(
    file: LoadState<RepoContent>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenInBrowser: (String) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (file is LoadState.Success) file.data.name else "File", maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            }
        )
        when (file) {
            is LoadState.Loading -> SectionLoading()
            is LoadState.Error -> ErrorState(file.message, onRetry)
            is LoadState.Success -> {
                val text = file.data.decodedText()
                if (text == null) {
                    Column(
                        Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Can't preview this file here \u2014 it may be binary or too large.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(12.dp))
                        val url = file.data.htmlUrl
                        if (url != null) {
                            TextButton(onClick = { onOpenInBrowser(url) }) { Text("Open on GitHub") }
                        }
                    }
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(text, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
