package mingsin.event

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/**
 * WebSocket 路由配置
 */
fun Application.configureWebSocket(sessionManager: SessionManager) {
    val logger = LoggerFactory.getLogger("mingsin.event.WebSocketRoutes")
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    routing {
        // App 客户端 WebSocket 端点（发送事件）
        webSocket("/ws/app") {
            logger.info("App client connected")
            sessionManager.addAppSession(this)
            
            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        logger.debug("Received event from app: $text")
                        
                        try {
                            val event = json.decodeFromString<Event>(text)
                            logger.info("Event received: id=${event.id}, type=${event.type}, source=${event.source}")
                            
                            // 广播事件到所有 Desktop 客户端
                            sessionManager.broadcastToDesktop(event)
                        } catch (e: Exception) {
                            logger.error("Failed to parse event: ${e.message}", e)
                            send(Frame.Text("Error: Invalid event format"))
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("WebSocket error for app client: ${e.message}", e)
            } finally {
                sessionManager.removeAppSession(this)
                logger.info("App client disconnected")
            }
        }
        
        // Desktop 客户端 WebSocket 端点（接收事件广播）
        webSocket("/ws/desktop") {
            logger.info("Desktop client connected")
            sessionManager.addDesktopSession(this)
            
            try {
                // 发送欢迎消息
                send(Frame.Text("Connected to EventTracker server"))
                
                // 保持连接，等待接收广播的事件
                for (frame in incoming) {
                    // Desktop 客户端通常只接收消息，不发送
                    // 但这里可以处理心跳或其他控制消息
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        logger.debug("Received message from desktop: $text")
                    }
                }
            } catch (e: Exception) {
                logger.error("WebSocket error for desktop client: ${e.message}", e)
            } finally {
                sessionManager.removeDesktopSession(this)
                logger.info("Desktop client disconnected")
            }
        }
    }
}

