package mingsin.event

import io.ktor.websocket.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/**
 * WebSocket session 管理器
 * 负责管理所有 WebSocket 连接，区分 App 客户端和 Desktop 客户端
 */
class SessionManager {
    private val logger = LoggerFactory.getLogger(SessionManager::class.java)
    private val mutex = Mutex()
    
    // App 客户端 session 列表（发送事件的客户端）
    private val appSessions = mutableSetOf<DefaultWebSocketSession>()
    
    // Desktop 客户端 session 列表（接收事件的客户端）
    private val desktopSessions = mutableSetOf<DefaultWebSocketSession>()
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * 添加 App 客户端 session
     */
    suspend fun addAppSession(session: DefaultWebSocketSession) {
        mutex.withLock {
            appSessions.add(session)
            logger.info("App session added. Total app sessions: ${appSessions.size}")
        }
    }
    
    /**
     * 添加 Desktop 客户端 session
     */
    suspend fun addDesktopSession(session: DefaultWebSocketSession) {
        mutex.withLock {
            desktopSessions.add(session)
            logger.info("Desktop session added. Total desktop sessions: ${desktopSessions.size}")
        }
    }
    
    /**
     * 移除 App 客户端 session
     */
    suspend fun removeAppSession(session: DefaultWebSocketSession) {
        mutex.withLock {
            appSessions.remove(session)
            logger.info("App session removed. Total app sessions: ${appSessions.size}")
        }
    }
    
    /**
     * 移除 Desktop 客户端 session
     */
    suspend fun removeDesktopSession(session: DefaultWebSocketSession) {
        mutex.withLock {
            desktopSessions.remove(session)
            logger.info("Desktop session removed. Total desktop sessions: ${desktopSessions.size}")
        }
    }
    
    /**
     * 向所有 Desktop 客户端广播事件
     * @param event 要广播的事件
     */
    suspend fun broadcastToDesktop(event: Event) {
        val eventJson = json.encodeToString(event)
        val frame = Frame.Text(eventJson)
        
        // 在锁内获取所有 session 的副本，然后在锁外发送消息
        val sessionsToBroadcast = mutex.withLock {
            desktopSessions.toList()
        }
        
        val sessionsToRemove = mutableSetOf<DefaultWebSocketSession>()
        
        // 在锁外发送消息，避免阻塞
        sessionsToBroadcast.forEach { session ->
            try {
                session.send(frame)
            } catch (e: Exception) {
                logger.warn("Failed to send event to desktop session: ${e.message}")
                sessionsToRemove.add(session)
            }
        }
        
        // 移除失效的 session
        if (sessionsToRemove.isNotEmpty()) {
            mutex.withLock {
                desktopSessions.removeAll(sessionsToRemove)
            }
        }
        
        val activeCount = mutex.withLock { desktopSessions.size }
        logger.debug("Broadcasted event ${event.id} to $activeCount desktop clients")
    }
    
    /**
     * 获取当前连接的客户端数量
     */
    suspend fun getSessionCounts(): Pair<Int, Int> {
        return mutex.withLock {
            Pair(appSessions.size, desktopSessions.size)
        }
    }
}

