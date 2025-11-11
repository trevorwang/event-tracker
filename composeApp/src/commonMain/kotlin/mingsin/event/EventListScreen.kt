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

/**
 * Event List 页面
 */
@Composable
fun EventListScreen(
    modifier: Modifier = Modifier,
    webSocketClient: WebSocketClient,
    onDisconnect: () -> Unit = {}
) {
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isConnected by remember { mutableStateOf(false) }
    
    // 监听 StateFlow
    LaunchedEffect(webSocketClient) {
        webSocketClient.events.collect { eventList ->
            events = eventList
        }
    }
    
    LaunchedEffect(webSocketClient) {
        webSocketClient.isConnected.collect { connected ->
            isConnected = connected
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "事件列表",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 连接状态指示器
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (isConnected) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = if (isConnected) "已连接" else "未连接",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                
                Button(onClick = onDisconnect) {
                    Text("断开连接")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 事件列表
        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无事件",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events.reversed()) { event ->
                    EventCard(event = event)
                }
            }
        }
    }
}

/**
 * 事件卡片组件
 */
@Composable
fun EventCard(event: Event) {
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
                Text(
                    text = event.type,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = event.source,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
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

/**
 * 格式化时间戳
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

