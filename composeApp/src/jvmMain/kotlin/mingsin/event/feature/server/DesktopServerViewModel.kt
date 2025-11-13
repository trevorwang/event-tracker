package mingsin.event.feature.server

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import mingsin.event.DesktopServerManager
import mingsin.event.SERVER_PORT
import mingsin.event.architecture.SimpleViewModel

class DesktopServerViewModel : SimpleViewModel() {

    private val _uiState = MutableStateFlow(DesktopServerState(portText = SERVER_PORT.toString()))
    val uiState: StateFlow<DesktopServerState> = _uiState.asStateFlow()

    val effects = MutableSharedFlow<DesktopServerEffect>()

    init {
        // Observe server manager state
        launch {
            DesktopServerManager.isStarting.collectLatest { isStarting ->
                _uiState.value = _uiState.value.copy(isStarting = isStarting)
            }
        }
        launch {
            DesktopServerManager.isRunning.collectLatest { isRunning ->
                _uiState.value = _uiState.value.copy(isRunning = isRunning)
            }
        }
        launch {
            DesktopServerManager.endpoints.collectLatest { endpoints ->
                _uiState.value = _uiState.value.copy(endpoints = endpoints)
            }
        }
        launch {
            DesktopServerManager.currentPort.collectLatest { port ->
                _uiState.value = _uiState.value.copy(currentPort = port)
            }
        }
    }

    fun dispatch(intent: DesktopServerIntent) {
        when (intent) {
            is DesktopServerIntent.UpdatePortText -> {
                val portError = validatePort(intent.text)
                _uiState.value = _uiState.value.copy(
                    portText = intent.text,
                    portError = portError
                )
            }
            DesktopServerIntent.StartServer -> {
                val currentState = _uiState.value
                val portError = validatePort(currentState.portText)
                if (portError == null) {
                    val port = currentState.portText.toIntOrNull()
                    if (port != null) {
                        DesktopServerManager.start(port)
                    }
                } else {
                    _uiState.value = currentState.copy(portError = portError)
                }
            }
            DesktopServerIntent.StopServer -> {
                DesktopServerManager.stop()
            }
        }
    }

    private fun validatePort(portText: String): String? {
        val port = portText.toIntOrNull()
        return if (port == null || port < 1 || port > 65535) {
            "Please enter a valid port number (1-65535)"
        } else {
            null
        }
    }
}

