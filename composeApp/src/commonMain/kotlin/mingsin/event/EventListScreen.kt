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
                
                // Clear all events button
                if (state.events.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { viewModel.dispatch(EventListIntent.ClearAllEvents) }
                    ) {
                        Text("Clear All")
                    }
                }
                
                Button(onClick = { viewModel.dispatch(EventListIntent.Disconnect); onDisconnect() }) {
                    Text("Disconnect")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filters
        EventFilters(
            selectedType = state.selectedType,
            selectedSource = state.selectedSource,
            selectedDeviceName = state.selectedDeviceName,
            availableTypes = state.availableTypes,
            availableSources = state.availableSources,
            availableDeviceNames = state.availableDeviceNames,
            onTypeSelected = { type ->
                viewModel.dispatch(EventListIntent.SetTypeFilter(type))
            },
            onSourceSelected = { source ->
                viewModel.dispatch(EventListIntent.SetSourceFilter(source))
            },
            onDeviceNameSelected = { deviceName ->
                viewModel.dispatch(EventListIntent.SetDeviceNameFilter(deviceName))
            },
            onClearFilters = {
                viewModel.dispatch(EventListIntent.ClearFilters)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Event list
        if (state.filteredEvents.isEmpty()) {
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
                items(state.filteredEvents.reversed(), key = { "${it.id}-${it.timestamp}" }) { event ->
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
 * Event Filters Component
 */
@Composable
fun EventFilters(
    selectedType: String?,
    selectedSource: String?,
    selectedDeviceName: String?,
    availableTypes: Set<String>,
    availableSources: Set<String>,
    availableDeviceNames: Set<String>,
    onTypeSelected: (String?) -> Unit,
    onSourceSelected: (String?) -> Unit,
    onDeviceNameSelected: (String?) -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (selectedType != null || selectedSource != null || selectedDeviceName != null) {
                    TextButton(onClick = onClearFilters) {
                        Text("Clear")
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type filter
                FilterDropdown(
                    label = "Type",
                    selectedValue = selectedType,
                    options = availableTypes.sorted(),
                    onValueSelected = onTypeSelected,
                    modifier = Modifier.weight(1f)
                )
                
                // Source filter
                FilterDropdown(
                    label = "Source",
                    selectedValue = selectedSource,
                    options = availableSources.sorted(),
                    onValueSelected = onSourceSelected,
                    modifier = Modifier.weight(1f)
                )
                
                // Device Name filter
                FilterDropdown(
                    label = "Device Name",
                    selectedValue = selectedDeviceName,
                    options = availableDeviceNames.sorted(),
                    onValueSelected = onDeviceNameSelected,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Filter Dropdown Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    selectedValue: String?,
    options: List<String>,
    onValueSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue ?: "All",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    onValueSelected(null)
                    expanded = false
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
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

