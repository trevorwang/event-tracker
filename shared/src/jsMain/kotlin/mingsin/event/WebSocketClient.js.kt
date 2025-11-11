package mingsin.event

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*

actual fun createHttpClient(): HttpClient {
    return HttpClient(Js) {
        install(WebSockets)
        install(ContentNegotiation) {
            json()
        }
    }
}

