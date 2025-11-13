package mingsin.event.feature.server

import mingsin.event.SERVER_PORT
import mingsin.event.architecture.UiEffect
import mingsin.event.architecture.UiIntent
import mingsin.event.architecture.UiState

data class DesktopServerState(
    val isStarting: Boolean = false,
    val isRunning: Boolean = false,
    val endpoints: List<String> = emptyList(),
    val currentPort: Int = SERVER_PORT,
    val portText: String = SERVER_PORT.toString(),
    val portError: String? = null
) : UiState

sealed interface DesktopServerIntent : UiIntent {
    data class UpdatePortText(val text: String) : DesktopServerIntent
    data object StartServer : DesktopServerIntent
    data object StopServer : DesktopServerIntent
}

sealed interface DesktopServerEffect : UiEffect {
    data class WebSocketConnected(val client: mingsin.event.WebSocketClient) : DesktopServerEffect
    data class WebSocketConnectionFailed(val error: String) : DesktopServerEffect
}

