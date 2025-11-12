package mingsin.event

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import mingsin.event.feature.list.EventListIntent
import mingsin.event.feature.list.EventListViewModel
import mingsin.event.feature.list.EventListState

/**
 * Event List Screen
 */
@Composable
fun EventListScreen(
    modifier: Modifier = Modifier,
    webSocketClient: WebSocketClient,
    onDisconnect: () -> Unit = {}
) {
    val viewModel = remember(webSocketClient) { EventListViewModel(webSocketClient) }
    val state by viewModel.uiState.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Event List",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Connection status indicator
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (state.isConnected) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = if (state.isConnected) "Connected" else "Disconnected",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                
                Button(onClick = { viewModel.dispatch(EventListIntent.Disconnect); onDisconnect() }) {
                    Text("Disconnect")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Event list
        if (state.events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No events",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.events.reversed(), key = { "${it.id}-${it.timestamp}" }) { event ->
                    val isExpanded = state.expandedIds.contains(event.id)
                    EventCard(
                        event = event,
                        isExpanded = isExpanded,
                        onToggle = { viewModel.dispatch(EventListIntent.ToggleExpand(event.id)) }
                    )
                }
            }
        }
    }
}

/**
 * Event Card Component
 */
@Composable
fun EventCard(
    event: Event,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val eventName = remember(event) {
        extractEventName(event.data) ?: event.name
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = eventName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " / ${event.type}, ${event.source}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(onClick = onToggle) {
                    Text(if (isExpanded) "Collapse" else "Expand")
                }
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = event.data?:"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "ID: ${event.id} | ${formatTimestamp(event.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Extract "name" field from JSON string without additional dependencies
 */
private fun extractEventName(data: String?): String? {
    if (data.isNullOrBlank()) return null
    // Extract "name":"..." with minimal overhead; not strict JSON parsing to avoid dependencies
    val regex = Regex(""""name"\s*:\s*"([^"]+)"""")
    return regex.find(data)?.groupValues?.getOrNull(1)
}

/**
 * Format timestamp
 */
expect fun formatTimestamp(timestamp: Long): String

@Preview
@Composable
private fun EventListScreenPreview() {
    MaterialTheme {
        EventListScreen(
            webSocketClient = WebSocketClient()
        )
    }
}

