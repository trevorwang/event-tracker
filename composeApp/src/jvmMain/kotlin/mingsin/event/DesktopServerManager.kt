package mingsin.event

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.response.respond
import io.ktor.server.request.receive
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.Inet4Address
import java.net.NetworkInterface
import kotlin.time.Duration.Companion.seconds

object DesktopServerManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null
    private val sessionManager = SessionManagerDesktop()

    private val _isStarting = MutableStateFlow(false)
    val isStarting: StateFlow<Boolean> = _isStarting

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _endpoints = MutableStateFlow<List<String>>(emptyList())
    val endpoints: StateFlow<List<String>> = _endpoints

    fun start() {
        if (_isStarting.value || _isRunning.value) return
        _isStarting.value = true
        scope.launch {
            try {
                val engine = startEmbeddedServer()
                server = engine
                _isRunning.value = true
                _endpoints.value = getLocalHttpAddresses()
            } finally {
                _isStarting.value = false
            }
        }
    }

    fun stop() {
        val toStop = server ?: return
        server = null
        _isRunning.value = false
        scope.launch {
            try {
                toStop.stop()
            } catch (_: Throwable) {
            }
            _endpoints.value = emptyList()
        }
    }

    private fun installModule(app: Application) {
        with(app) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        encodeDefaults = true
                        prettyPrint = true
                    }
                )
            }
            install(WebSockets) {
                pingPeriod = 15.seconds
                timeout = 15.seconds
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }
            routing {
                get("/") {
                    call.respondText(
                        "EventTracker Desktop Embedded Server\n" +
                            "Endpoints:\n" +
                            "  REST:\n" +
                            "    - GET /api/status\n" +
                            "    - POST /api/events\n" +
                            "  WebSocket:\n" +
                            "    - /ws/app (send events)\n" +
                            "    - /ws/desktop (receive events)\n"
                    )
                }
                get("/api/status") {
                    call.respondText("OK")
                }
                // REST: Receive events and broadcast
                post("/api/events") {
                    try {
                        val event = call.receive<Event>()
                        sessionManager.broadcastToDesktop(event)
                        call.respond(
                            io.ktor.http.HttpStatusCode.Created,
                            EventResponse(
                                success = true,
                                message = "Event received and broadcasted",
                                eventId = event.id
                            )
                        )
                    } catch (e: Exception) {
                        call.respond(
                            io.ktor.http.HttpStatusCode.BadRequest,
                            EventResponse(
                                success = false,
                                message = "Invalid event format: ${e.message}",
                                eventId = ""
                            )
                        )
                    }
                }
                // REST: Status
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
                // WebSocket: App sends events
                webSocket("/ws/app") {
                    sessionManager.addAppSession(this)
                    try {
                        for (frame in incoming) {
                            if (frame is io.ktor.websocket.Frame.Text) {
                                val text = frame.readText()
                                try {
                                    val event = kotlinx.serialization.json.Json {
                                        ignoreUnknownKeys = true
                                        encodeDefaults = true
                                    }.decodeFromString<Event>(text)
                                    sessionManager.broadcastToDesktop(event)
                                } catch (_: Exception) {
                                    send(io.ktor.websocket.Frame.Text("Error: Invalid event format"))
                                }
                            }
                        }
                    } catch (_: Exception) {
                    } finally {
                        sessionManager.removeAppSession(this)
                    }
                }
                // WebSocket: Desktop receives events
               webSocket("/ws/desktop") {
                    sessionManager.addDesktopSession(this)
                   try {
                       // Send welcome message
                       send(Frame.Text("Connected to EventTracker server"))

                       // Keep connection alive, waiting to receive broadcast events
                       for (frame in incoming) {
                           // Desktop clients typically only receive messages, not send
                           // But here we can handle heartbeat or other control messages
                           if (frame is Frame.Text) {
                               val text = frame.readText()
//                               logger.debug("Received message from desktop: $text")
                           }
                       }
                   } catch (e: Exception) {
//                       logger.error("WebSocket error for desktop client: ${e.message}", e)
                   } finally {
                       sessionManager.removeDesktopSession(this)
//                       logger.info("Desktop client disconnected")
                   }
                }
            }
        }
    }

    private fun startEmbeddedServer(): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
        return embeddedServer(
            Netty,
            port = SERVER_PORT,
            host = "0.0.0.0",
            module = { installModule(this) }
        ).start(wait = false)
    }

    private fun getLocalHttpAddresses(): List<String> {
        val addresses = mutableListOf<String>()
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val nif = interfaces.nextElement()
            if (!nif.isUp || nif.isLoopback || nif.isVirtual) continue
            val inetAddresses = nif.inetAddresses
            while (inetAddresses.hasMoreElements()) {
                val addr = inetAddresses.nextElement()
                if (addr is Inet4Address && !addr.isLoopbackAddress && !addr.isLinkLocalAddress) {
                    addresses.add("http://${addr.hostAddress}:$SERVER_PORT")
                }
            }
        }
        addresses.add("http://localhost:$SERVER_PORT")
        return addresses.distinct()
    }
}


