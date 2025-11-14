package mingsin.event

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import mingsin.event.logger.FileLogWriterFactory

/**
 * WebSocket client manager
 */
class WebSocketClient {
    private val logger = FileLogWriterFactory().create()
    private val client = createHttpClient()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var session: DefaultWebSocketSession? = null
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()
    
    /**
     * Connect to WebSocket server
     * @param url WebSocket server URL
     * @return Whether the connection was successful
     */
    suspend fun connect(url: String): Result<Unit> {
        return try {
            logger.i { "Connecting to WebSocket server: $url" }
            val newSession = client.webSocketSession(url)
            session = newSession
            _isConnected.value = true
            logger.i { "Connected to WebSocket server: $url" }

            // Listen for messages in background
            scope.launch {
                try {
                    for (frame in newSession.incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            logger.d { "Received message: $text" }
                            try {
                                val event = json.decodeFromString<Event>(text)
                                _events.value += event
                                logger.d { "Event parsed and added: id=${event.id}, type=${event.type}" }
                            } catch (e: Exception) {
                                logger.w(e) { "Failed to parse message as Event: ${e.message}" }
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.e(e) { "Connection exception/closed: ${e.message}" }
                } finally {
                    _isConnected.value = false
                    session = null
                    logger.i { "Disconnected from WebSocket server" }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(e) { "Failed to connect to WebSocket server: ${e.message}" }
            _isConnected.value = false
            session = null
            Result.failure(e)
        }
    }
    
    /**
     * Disconnect from server
     */
    suspend fun disconnect() {
        logger.i { "Disconnecting from WebSocket server" }
        session?.close()
        session = null
        _isConnected.value = false
        logger.i { "Disconnected from WebSocket server" }
    }
    
    /**
     * Check if connected
     */
    fun isConnected(): Boolean {
        return _isConnected.value
    }
    
    /**
     * Receive event stream
     */
    fun receiveEvents(): Flow<Event> = flow {
        _events.collect { eventList ->
            eventList.forEach { event ->
                emit(event)
            }
        }
    }
    
    /**
     * Clear all events
     */
    fun clearEvents() {
        logger.i { "Clearing all events" }
        _events.value = emptyList()
        logger.i { "All events cleared" }
    }
    
    /**
     * Close client
     */
    fun close() {
        logger.i { "Closing WebSocket client" }
        scope.launch {
            disconnect()
        }
        client.close()
        logger.i { "WebSocket client closed" }
    }
}

/**
 * Create platform-specific HttpClient
 */
expect fun createHttpClient(): HttpClient

