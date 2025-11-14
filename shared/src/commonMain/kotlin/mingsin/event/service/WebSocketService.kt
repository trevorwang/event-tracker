package mingsin.event.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import mingsin.event.WebSocketClient
import mingsin.event.logger.FileLogWriterFactory

/**
 * WebSocket Service
 * Manages WebSocket client lifecycle and connection state
 */
class WebSocketService {
    private val logger = FileLogWriterFactory().create()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val webSocketClient = WebSocketClient()
    
    /**
     * Get WebSocket client instance
     */
    fun getWebSocketClient(): WebSocketClient {
        return webSocketClient
    }
    
    /**
     * Get connection state
     */
    val isConnected: StateFlow<Boolean> = webSocketClient.isConnected
    
    /**
     * Get events flow
     */
    val events = webSocketClient.events
    
    /**
     * Connect to WebSocket server
     * @param url WebSocket server URL
     * @return Result indicating success or failure
     */
    suspend fun connect(url: String): Result<Unit> {
        logger.i { "Connecting to WebSocket: $url" }
        return webSocketClient.connect(url)
    }
    
    /**
     * Disconnect from WebSocket server
     */
    suspend fun disconnect() {
        logger.i { "Disconnecting from WebSocket" }
        webSocketClient.disconnect()
    }
    
    /**
     * Close WebSocket client
     */
    fun close() {
        logger.i { "Closing WebSocket client" }
        webSocketClient.close()
    }
    
    /**
     * Clear all events
     */
    fun clearEvents() {
        webSocketClient.clearEvents()
    }
}

