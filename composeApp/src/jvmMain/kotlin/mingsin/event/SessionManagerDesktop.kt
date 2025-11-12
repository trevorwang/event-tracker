package mingsin.event

import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * WebSocket session manager for desktop embedded server
 */
class SessionManagerDesktop {
    private val mutex = Mutex()
    private val appSessions = mutableSetOf<DefaultWebSocketSession>()
    private val desktopSessions = mutableSetOf<DefaultWebSocketSession>()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun addAppSession(session: DefaultWebSocketSession) {
        mutex.withLock { appSessions.add(session) }
    }

    suspend fun addDesktopSession(session: DefaultWebSocketSession) {
        mutex.withLock { desktopSessions.add(session) }
    }

    suspend fun removeAppSession(session: DefaultWebSocketSession) {
        mutex.withLock { appSessions.remove(session) }
    }

    suspend fun removeDesktopSession(session: DefaultWebSocketSession) {
        mutex.withLock { desktopSessions.remove(session) }
    }

    suspend fun broadcastToDesktop(event: Event) {
        val payload = json.encodeToString(event)
        val sessions = mutex.withLock { desktopSessions.toList() }
        sessions.forEach { session ->
            try {
                session.send(Frame.Text(payload))
            } catch (_: Throwable) {
                // ignore failed session send
            }
        }
    }

    suspend fun getSessionCounts(): Pair<Int, Int> {
        return mutex.withLock { appSessions.size to desktopSessions.size }
    }
}


