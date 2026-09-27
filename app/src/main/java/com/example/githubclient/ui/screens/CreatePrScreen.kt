package com.example.githubclient.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.githubclient.data.model.BranchRef
import com.example.githubclient.ui.components.ExplainerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePrScreen(
    repoFullName: String,
    branches: LoadState<List<BranchRef>>,
    isSubmitting: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSubmit: (title: String, body: String, head: String, base: String, draft: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var head by remember { mutableStateOf<String?>(null) }
    var base by remember { mutableStateOf<String?>(null) }
    var draft by remember { mutableStateOf(false) }

    val branchNames = (branches as? LoadState.Success)?.data?.map { it.name } ?: emptyList()
    LaunchedEffect(branchNames) {
        if (base == null && branchNames.isNotEmpty()) {
            base = branchNames.firstOrNull { it == "main" || it == "master" } ?: branchNames.first()
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("New Pull Request \u00b7 $repoFullName", maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            }
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExplainerCard(
                title = "Opening a pull request",
                body = "Choose the branch with your changes (\"compare\") and the branch you want to merge them into " +
                    "(\"base\"). GitHub will show the diff once it's created, and you can merge it from there."
            )
            when (branches) {
                is LoadState.Loading -> Text("Loading branches\u2026", style = MaterialTheme.typography.bodyMedium)
                is LoadState.Error -> Text(branches.message, color = MaterialTheme.colorScheme.error)
                is LoadState.Success -> {
                    BranchDropdown(label = "Compare (head)", options = branchNames, selected = head, onSelected = { head = it })
                    BranchDropdown(label = "Base", options = branchNames, selected = base, onSelected = { base = it })
                }
            }
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Description (optional)") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(140.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = draft, onCheckedChange = { draft = it })
                Text("Open as draft")
            }
            if (errorMessage != null) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = { if (head != null && base != null) onSubmit(title, body, head!!, base!!, draft) },
                enabled = title.isNotBlank() && head != null && base != null && head != base && !isSubmitting,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Create pull request")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BranchDropdown(label: String, options: List<String>, selected: String?, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelected(option); expanded = false })
            }
        }
    }
}
