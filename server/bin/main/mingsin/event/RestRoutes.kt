package mingsin.event

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

/**
 * RESTful API 路由配置
 */
fun Application.configureRestApi(sessionManager: SessionManager) {
    val logger = LoggerFactory.getLogger("mingsin.event.RestRoutes")
    
    routing {
        // POST /api/events - 接收来自 App 的事件
        post("/api/events") {
            try {
                val event = call.receive<Event>()
                logger.info("Event received via REST: id=${event.id}, type=${event.type}, source=${event.source}")
                
                // 广播事件到所有 Desktop 客户端
                sessionManager.broadcastToDesktop(event)
                
                // 返回成功响应
                call.respond(
                    HttpStatusCode.Created,
                    EventResponse(
                        success = true,
                        message = "Event received and broadcasted",
                        eventId = event.id
                    )
                )
            } catch (e: Exception) {
                logger.error("Failed to process event: ${e.message}", e)
                call.respond(
                    HttpStatusCode.BadRequest,
                    EventResponse(
                        success = false,
                        message = "Invalid event format: ${e.message}",
                        eventId = ""
                    )
                )
            }
        }
        
        // GET /api/status - 获取服务器状态
        get("/api/status") {
            val (appCount, desktopCount) = sessionManager.getSessionCounts()
            call.respond(
                StatusResponse(
                    status = "running",
                    appClients = appCount,
                    desktopClients = desktopCount
                )
            )
        }
    }
}

