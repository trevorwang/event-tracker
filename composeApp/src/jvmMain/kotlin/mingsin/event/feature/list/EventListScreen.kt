package mingsin.event.feature.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import mingsin.event.Event
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Event List Screen
 */
@Composable
fun EventListScreen(
    modifier: Modifier = Modifier,
    onDisconnect: () -> Unit = {}
) {
    val viewModel: EventListViewModel = koinInject()
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

                val jsonTree = remember(event.data) {
                    parseJsonToTree(event.data)
                }
                val expandedNodesState = remember(event.id, jsonTree) {
                    val allPaths = jsonTree?.let { collectAllPaths(it) } ?: setOf("root")
                    mutableStateOf(allPaths)
                }

                if (jsonTree != null) {
                    JsonTreeView(
                        node = jsonTree,
                        expandedNodes = expandedNodesState.value,
                        onExpandedNodesChange = { expandedNodesState.value = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = event.data ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

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

fun formatJson(data: String): String {
    return try {
        // Try to parse as JSON
        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            isLenient = true
        }
        val jsonElement = json.parseToJsonElement(data)
        // Format JSON with pretty print
        json.encodeToString(JsonElement.serializer(), jsonElement)
    } catch (e: Exception) {
        // If not valid JSON, return empty string to use plain text display
        ""
    }
}

/**
 * JSON Tree Node data structure
 */
sealed class JsonTreeNode {
    abstract val key: String?
    abstract val value: JsonElement
    
    data class ObjectNode(
        override val key: String?,
        override val value: JsonObject,
        val children: List<JsonTreeNode>
    ) : JsonTreeNode()
    
    data class ArrayNode(
        override val key: String?,
        override val value: JsonArray,
        val children: List<JsonTreeNode>
    ) : JsonTreeNode()
    
    data class PrimitiveNode(
        override val key: String?,
        override val value: JsonPrimitive
    ) : JsonTreeNode()
}

/**
 * Parse JSON string to tree structure
 */
private fun parseJsonToTree(data: String?): JsonTreeNode? {
    if (data.isNullOrBlank()) return null
    
    return try {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        val jsonElement = json.parseToJsonElement(data)
        buildTreeNode(null, jsonElement)
    } catch (e: Exception) {
        null
    }
}

/**
 * Get node type order for sorting (0 = object/primitive, 1 = array)
 */
private fun getNodeTypeOrder(node: JsonTreeNode): Int {
    return when (node) {
        is JsonTreeNode.ArrayNode -> 1
        else -> 0
    }
}

/**
 * Build tree node from JSON element
 */
private fun buildTreeNode(key: String?, element: JsonElement): JsonTreeNode {
    return when (element) {
        is JsonObject -> {
            val children = element.entries.map { entry ->
                buildTreeNode(entry.key, entry.value)
            }.sortedWith(compareBy(
                { getNodeTypeOrder(it) }, // Arrays go last
                { it.key ?: "" } // Then alphabetically by key
            ))
            JsonTreeNode.ObjectNode(key, element, children)
        }
        is JsonArray -> {
            val children = element.mapIndexed { index, item ->
                buildTreeNode(index.toString(), item)
            }
            JsonTreeNode.ArrayNode(key, element, children)
        }
        is JsonPrimitive -> {
            JsonTreeNode.PrimitiveNode(key, element)
        }
    }
}

/**
 * Collect all node paths recursively for default expansion
 */
private fun collectAllPaths(node: JsonTreeNode, path: String = "root", paths: MutableSet<String> = mutableSetOf()): Set<String> {
    paths.add(path)
    when (node) {
        is JsonTreeNode.ObjectNode -> {
            node.children.forEachIndexed { index, child ->
                val childPath = if (path == "root") child.key ?: "" else "$path.${child.key}"
                collectAllPaths(child, childPath, paths)
            }
        }
        is JsonTreeNode.ArrayNode -> {
            node.children.forEachIndexed { index, child ->
                val childPath = "$path[$index]"
                collectAllPaths(child, childPath, paths)
            }
        }
        is JsonTreeNode.PrimitiveNode -> {
            // Leaf node, no children
        }
    }
    return paths
}

/**
 * Format JSON primitive value
 */
private fun formatJsonPrimitive(primitive: JsonPrimitive): String {
    val content = primitive.content
    return when {
        primitive.isString -> "\"$content\""
        primitive.booleanOrNull != null -> content
        primitive.longOrNull != null -> content
        primitive.doubleOrNull != null -> content
        content == "null" -> "null"
        else -> content
    }
}

/**
 * JSON Tree View Component
 */
@Composable
fun JsonTreeView(
    node: JsonTreeNode,
    modifier: Modifier = Modifier,
    expandedNodes: Set<String>,
    onExpandedNodesChange: (Set<String>) -> Unit,
    path: String = "root",
    prefix: String = "",
    isLast: Boolean = true
) {
    Column(modifier = modifier) {
        when (node) {
            is JsonTreeNode.ObjectNode -> {
                if (path == "root") {
                    // Root node
                    JsonTreeNodeRow(
                        label = "root",
                        isExpanded = expandedNodes.contains(path),
                        hasChildren = node.children.isNotEmpty(),
                        onToggle = {
                            val newSet = expandedNodes.toMutableSet()
                            if (newSet.contains(path)) {
                                newSet.remove(path)
                            } else {
                                newSet.add(path)
                            }
                            onExpandedNodesChange(newSet)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (expandedNodes.contains(path)) {
                        node.children.forEachIndexed { index, child ->
                            val isLastChild = index == node.children.size - 1
                            val childPath = child.key ?: ""
                            JsonTreeView(
                                node = child,
                                expandedNodes = expandedNodes,
                                onExpandedNodesChange = onExpandedNodesChange,
                                path = childPath,
                                prefix = "",
                                isLast = isLastChild,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    // Nested object
                    val connector = if (isLast) "└── " else "├── "
                    val label = "\"${node.key}\": { Object } (${node.value.size} keys)"
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = prefix,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        JsonTreeNodeRow(
                            label = "$connector$label",
                            isExpanded = expandedNodes.contains(path),
                            hasChildren = node.children.isNotEmpty(),
                            onToggle = {
                                val newSet = expandedNodes.toMutableSet()
                                if (newSet.contains(path)) {
                                    newSet.remove(path)
                                } else {
                                    newSet.add(path)
                                }
                                onExpandedNodesChange(newSet)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    if (expandedNodes.contains(path)) {
                        val nextPrefix = if (isLast) "$prefix    " else "$prefix│   "
                        node.children.forEachIndexed { index, child ->
                            val isLastChild = index == node.children.size - 1
                            val childPath = "$path.${child.key}"
                            JsonTreeView(
                                node = child,
                                expandedNodes = expandedNodes,
                                onExpandedNodesChange = onExpandedNodesChange,
                                path = childPath,
                                prefix = nextPrefix,
                                isLast = isLastChild,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            is JsonTreeNode.ArrayNode -> {
                val connector = if (isLast) "└── " else "├── "
                val label = "\"${node.key}\": [ Array ] (${node.value.size} items)"
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = prefix,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    JsonTreeNodeRow(
                        label = "$connector$label",
                        isExpanded = expandedNodes.contains(path),
                        hasChildren = node.children.isNotEmpty(),
                        onToggle = {
                            val newSet = expandedNodes.toMutableSet()
                            if (newSet.contains(path)) {
                                newSet.remove(path)
                            } else {
                                newSet.add(path)
                            }
                            onExpandedNodesChange(newSet)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                if (expandedNodes.contains(path)) {
                    val nextPrefix = if (isLast) "$prefix    " else "$prefix│   "
                    node.children.forEachIndexed { index, child ->
                        val isLastChild = index == node.children.size - 1
                        val childPath = "$path[$index]"
                        JsonTreeView(
                            node = child,
                            expandedNodes = expandedNodes,
                            onExpandedNodesChange = onExpandedNodesChange,
                            path = childPath,
                            prefix = nextPrefix,
                            isLast = isLastChild,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            is JsonTreeNode.PrimitiveNode -> {
                val connector = if (isLast) "└── " else "├── "
                val label = if (node.key != null && node.key?.matches(Regex("\\d+")) == true) {
                    // Array index
                    "[${node.key}]: ${formatJsonPrimitive(node.value)}"
                } else if (node.key != null) {
                    "\"${node.key}\": ${formatJsonPrimitive(node.value)}"
                } else {
                    formatJsonPrimitive(node.value)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = prefix,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$connector$label",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * JSON Tree Node Row Component
 */
@Composable
private fun JsonTreeNodeRow(
    label: String,
    isExpanded: Boolean,
    hasChildren: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable(enabled = hasChildren) { onToggle() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        if (hasChildren) {
            Text(
                text = if (isExpanded) "▼" else "▶",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 4.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Format timestamp
 */
fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    return format.format(date)
}

@Preview
@Composable
private fun EventListScreenPreview() {
    MaterialTheme {
        EventListScreen()
    }
}

