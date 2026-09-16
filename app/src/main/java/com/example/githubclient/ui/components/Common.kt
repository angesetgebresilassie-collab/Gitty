package com.example.githubclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.githubclient.ui.theme.*

enum class ItemStatus { OPEN, CLOSED, MERGED, DRAFT }

private data class PillStyle(
    val background: Color,
    val foreground: Color,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun StatusPill(status: ItemStatus, modifier: Modifier = Modifier) {
    val style = when (status) {
        ItemStatus.OPEN -> PillStyle(OpenGreenContainer, OpenGreen, "Open", Icons.Filled.Circle)
        ItemStatus.CLOSED -> PillStyle(ClosedRedContainer, ClosedRed, "Closed", Icons.Filled.CheckCircle)
        ItemStatus.MERGED -> PillStyle(MergedPurpleContainer, MergedPurple, "Merged", Icons.Filled.MergeType)
        ItemStatus.DRAFT -> PillStyle(DraftGrayContainer, DraftGray, "Draft", Icons.Filled.Circle)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(style.background)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Icon(style.icon, contentDescription = null, tint = style.foreground, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(5.dp))
        Text(style.label, color = style.foreground, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun LabelPill(name: String, colorHex: String, modifier: Modifier = Modifier) {
    val color = try {
        Color(android.graphics.Color.parseColor("#$colorHex"))
    } catch (e: Exception) {
        Color.Gray
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(name, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun Avatar(url: String?, size: Int = 32, modifier: Modifier = Modifier) {
    AsyncImage(
        model = url,
        contentDescription = "Avatar",
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

/**
 * A dismissible, collapsible "what does this mean" card.
 * Used at the top of the Issues and Pull Requests screens for people
 * who aren't already fluent in GitHub jargon.
 */
@Composable
fun ExplainerCard(title: String, body: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(true) }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Hide" else "Show")
                }
            }
            if (expanded) {
                Spacer(Modifier.height(6.dp))
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SectionLoading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Something went wrong", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        FilledTonalButton(onClick = onRetry) { Text("Retry") }
    }
}
