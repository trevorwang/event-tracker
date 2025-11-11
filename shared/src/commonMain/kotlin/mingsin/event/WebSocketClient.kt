package mingsin.event

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * WebSocket 客户端管理器
 */
class WebSocketClient {
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
     * 连接到 WebSocket 服务器
     * @param url WebSocket 服务器地址
     * @return 连接是否成功
     */
    suspend fun connect(url: String): Result<Unit> {
        return try {
            val newSession = client.webSocketSession(url)
            session = newSession
            _isConnected.value = true

            // 在后台监听消息
            scope.launch {
                try {
                    for (frame in newSession.incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            try {
                                val event = json.decodeFromString<Event>(text)
                                _events.value = _events.value + event
                            } catch (_: Exception) {
                                // 忽略无法解析的消息
                            }
                        }
                    }
                } catch (_: Exception) {
                    // 连接异常/关闭
                } finally {
                    _isConnected.value = false
                    session = null
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            _isConnected.value = false
            session = null
            Result.failure(e)
        }
    }
    
    /**
     * 断开连接
     */
    suspend fun disconnect() {
        session?.close()
        session = null
        _isConnected.value = false
    }
    
    /**
     * 检查是否已连接
     */
    fun isConnected(): Boolean {
        return _isConnected.value
    }
    
    /**
     * 接收事件流
     */
    fun receiveEvents(): Flow<Event> = flow {
        _events.collect { eventList ->
            eventList.forEach { event ->
                emit(event)
            }
        }
    }
    
    /**
     * 关闭客户端
     */
    fun close() {
        scope.launch {
            disconnect()
        }
        client.close()
    }
}

/**
 * 创建平台特定的 HttpClient
 */
expect fun createHttpClient(): HttpClient

