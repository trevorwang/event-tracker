package mingsin.event.service

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import mingsin.event.DesktopServerManager

/**
 * Desktop WebSocket Service
 * Manages WebSocket connection for desktop server
 * Auto-connects when server starts, disconnects when server stops
 */
class DesktopWebSocketService(
    private val webSocketService: WebSocketService
) {
    private val logger = Logger.withTag("DesktopWebSocketService")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    init {
        // Auto connect/disconnect based on server state
        scope.launch {
            DesktopServerManager.isRunning.collectLatest { isRunning ->
                if (isRunning) {
                    connectToServer()
                } else {
                    disconnectFromServer()
                }
            }
        }
        
        // Disconnect when endpoints become empty
        scope.launch {
            DesktopServerManager.endpoints.collectLatest { endpoints ->
                if (endpoints.isEmpty() && webSocketService.isConnected.value) {
                    disconnectFromServer()
                }
            }
        }
    }
    
    private suspend fun connectToServer() {
        val currentPort = DesktopServerManager.currentPort.value
        val wsUrl = "ws://localhost:$currentPort/ws/desktop"
        logger.i { "Auto-connecting to WebSocket: $wsUrl" }
        webSocketService.connect(wsUrl).onFailure { error ->
            logger.e(error) { "Failed to auto-connect WebSocket: ${error.message}" }
        }.onSuccess {
            logger.i { "WebSocket auto-connected successfully" }
        }
    }
    
    private suspend fun disconnectFromServer() {
        if (webSocketService.isConnected.value) {
            logger.i { "Auto-disconnecting from WebSocket" }
            webSocketService.disconnect()
        }
    }
    
    /**
     * Get WebSocket client instance
     */
    fun getWebSocketClient() = webSocketService.getWebSocketClient()
    
    /**
     * Get connection state
     */
    val isConnected = webSocketService.isConnected
    
    /**
     * Get events flow
     */
    val events = webSocketService.events
}

