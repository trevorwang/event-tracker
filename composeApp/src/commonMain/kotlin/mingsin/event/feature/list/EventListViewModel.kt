package mingsin.event.feature.list

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import mingsin.event.WebSocketClient
import mingsin.event.architecture.SimpleViewModel

class EventListViewModel(
    private val webSocketClient: WebSocketClient
) : SimpleViewModel() {

    private val _uiState = MutableStateFlow(EventListState())
    val uiState: StateFlow<EventListState> = _uiState.asStateFlow()

    val effects = MutableSharedFlow<EventListEffect>()

    init {
        // Bind event stream
        launch {
            webSocketClient.events.collectLatest { events ->
                _uiState.value = _uiState.value.copy(events = events)
            }
        }
        // Bind connection status
        launch {
            webSocketClient.isConnected.collectLatest { connected ->
                _uiState.value = _uiState.value.copy(isConnected = connected)
            }
        }
    }

    fun dispatch(intent: EventListIntent) {
        when (intent) {
            is EventListIntent.ToggleExpand -> {
                val current = _uiState.value
                val updated = current.expandedIds.toMutableSet().also { set ->
                    if (set.contains(intent.eventId)) set.remove(intent.eventId) else set.add(intent.eventId)
                }.toSet()
                _uiState.value = current.copy(expandedIds = updated)
            }
            EventListIntent.Disconnect -> {
                launch {
                    webSocketClient.disconnect()
                    effects.emit(EventListEffect.Disconnected)
                }
            }
        }
    }
}


