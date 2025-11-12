package mingsin.event.feature.list

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import mingsin.event.Event
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
                updateStateWithEvents(events)
            }
        }
        // Bind connection status
        launch {
            webSocketClient.isConnected.collectLatest { connected ->
                _uiState.value = _uiState.value.copy(isConnected = connected)
            }
        }
    }
    
    private fun updateStateWithEvents(events: List<Event>) {
        val availableTypes = events.map { it.type }.distinct().toSet()
        val availableSources = events.map { it.source }.distinct().toSet()
        val filteredEvents = applyFilters(events)
        
        _uiState.value = _uiState.value.copy(
            events = events,
            availableTypes = availableTypes,
            availableSources = availableSources,
            filteredEvents = filteredEvents
        )
    }
    
    private fun applyFilters(events: List<Event>): List<Event> {
        return events.filter { event ->
            val typeMatch = _uiState.value.selectedType == null || event.type == _uiState.value.selectedType
            val sourceMatch = _uiState.value.selectedSource == null || event.source == _uiState.value.selectedSource
            typeMatch && sourceMatch
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
            is EventListIntent.SetTypeFilter -> {
                val current = _uiState.value
                val filteredEvents = if (intent.type == null) {
                    applyFilters(current.events, null, current.selectedSource)
                } else {
                    applyFilters(current.events, intent.type, current.selectedSource)
                }
                _uiState.value = current.copy(
                    selectedType = intent.type,
                    filteredEvents = filteredEvents
                )
            }
            is EventListIntent.SetSourceFilter -> {
                val current = _uiState.value
                val filteredEvents = if (intent.source == null) {
                    applyFilters(current.events, current.selectedType, null)
                } else {
                    applyFilters(current.events, current.selectedType, intent.source)
                }
                _uiState.value = current.copy(
                    selectedSource = intent.source,
                    filteredEvents = filteredEvents
                )
            }
            EventListIntent.ClearFilters -> {
                val current = _uiState.value
                val filteredEvents = applyFilters(current.events, null, null)
                _uiState.value = current.copy(
                    selectedType = null,
                    selectedSource = null,
                    filteredEvents = filteredEvents
                )
            }
        }
    }
    
    private fun applyFilters(events: List<Event>, type: String?, source: String?): List<Event> {
        return events.filter { event ->
            val typeMatch = type == null || event.type == type
            val sourceMatch = source == null || event.source == source
            typeMatch && sourceMatch
        }
    }
}


