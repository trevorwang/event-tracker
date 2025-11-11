package mingsin.event

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // 配置 JSON 序列化
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = true
        })
    }
    
    // 配置 WebSocket
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    
    // 创建 SessionManager 实例
    val sessionManager = SessionManager()
    
    // 配置路由
    routing {
        get("/") {
            call.respondText("EventTracker Server\n" +
                    "Endpoints:\n" +
                    "  WebSocket:\n" +
                    "    - /ws/app: Connect as App client (send events)\n" +
                    "    - /ws/desktop: Connect as Desktop client (receive events)\n" +
                    "  RESTful API:\n" +
                    "    - POST /api/events: Send event from App\n" +
                    "    - GET /api/status: Get server status")
        }
        
        // 配置 RESTful API 路由
        configureRestApi(sessionManager)
        
        // 配置 WebSocket 路由
        configureWebSocket(sessionManager)
    }
}