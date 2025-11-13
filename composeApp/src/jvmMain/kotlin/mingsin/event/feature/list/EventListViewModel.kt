package mingsin.event.feature.list

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import mingsin.event.Event
import mingsin.event.service.DesktopWebSocketService
import mingsin.event.architecture.SimpleViewModel

class EventListViewModel(
    private val desktopWebSocketService: DesktopWebSocketService
) : SimpleViewModel() {

    private val _uiState = MutableStateFlow(EventListState())
    val uiState: StateFlow<EventListState> = _uiState.asStateFlow()

    val effects = MutableSharedFlow<EventListEffect>()

    init {
        // Bind event stream
        launch {
            desktopWebSocketService.events.collectLatest { events ->
                updateStateWithEvents(events)
            }
        }
        // Bind connection status
        launch {
            desktopWebSocketService.isConnected.collectLatest { connected ->
                _uiState.value = _uiState.value.copy(isConnected = connected)
            }
        }
    }
    
    private fun updateStateWithEvents(events: List<Event>) {
        val availableTypes = events.map { it.type }.distinct().toSet()
        val availableSources = events.map { it.source }.distinct().toSet()
        val availableDeviceNames = events.mapNotNull { it.deviceName }.distinct().toSet()
        val filteredEvents = applyFilters(events)
        
        _uiState.value = _uiState.value.copy(
            events = events,
            availableTypes = availableTypes,
            availableSources = availableSources,
            availableDeviceNames = availableDeviceNames,
            filteredEvents = filteredEvents
        )
    }
    
    private fun applyFilters(events: List<Event>): List<Event> {
        return events.filter { event ->
            val typeMatch = _uiState.value.selectedType == null || event.type == _uiState.value.selectedType
            val sourceMatch = _uiState.value.selectedSource == null || event.source == _uiState.value.selectedSource
            val deviceNameMatch = _uiState.value.selectedDeviceName == null || event.deviceName == _uiState.value.selectedDeviceName
            typeMatch && sourceMatch && deviceNameMatch
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
                    desktopWebSocketService.getWebSocketClient().disconnect()
                    effects.emit(EventListEffect.Disconnected)
                }
            }
            is EventListIntent.SetTypeFilter -> {
                val current = _uiState.value
                val filteredEvents = applyFilters(
                    current.events,
                    intent.type,
                    current.selectedSource,
                    current.selectedDeviceName
                )
                _uiState.value = current.copy(
                    selectedType = intent.type,
                    filteredEvents = filteredEvents
                )
            }
            is EventListIntent.SetSourceFilter -> {
                val current = _uiState.value
                val filteredEvents = applyFilters(
                    current.events,
                    current.selectedType,
                    intent.source,
                    current.selectedDeviceName
                )
                _uiState.value = current.copy(
                    selectedSource = intent.source,
                    filteredEvents = filteredEvents
                )
            }
            is EventListIntent.SetDeviceNameFilter -> {
                val current = _uiState.value
                val filteredEvents = applyFilters(
                    current.events,
                    current.selectedType,
                    current.selectedSource,
                    intent.deviceName
                )
                _uiState.value = current.copy(
                    selectedDeviceName = intent.deviceName,
                    filteredEvents = filteredEvents
                )
            }
            EventListIntent.ClearFilters -> {
                val current = _uiState.value
                val filteredEvents = applyFilters(current.events, null, null, null)
                _uiState.value = current.copy(
                    selectedType = null,
                    selectedSource = null,
                    selectedDeviceName = null,
                    filteredEvents = filteredEvents
                )
            }
            EventListIntent.ClearAllEvents -> {
                desktopWebSocketService.getWebSocketClient().clearEvents()
            }
        }
    }
    
    private fun applyFilters(
        events: List<Event>,
        type: String?,
        source: String?,
        deviceName: String?
    ): List<Event> {
        return events.filter { event ->
            val typeMatch = type == null || event.type == type
            val sourceMatch = source == null || event.source == source
            val deviceNameMatch = deviceName == null || event.deviceName == deviceName
            typeMatch && sourceMatch && deviceNameMatch
        }
    }
}


