package mingsin.event.feature.list

import mingsin.event.Event
import mingsin.event.architecture.UiEffect
import mingsin.event.architecture.UiIntent
import mingsin.event.architecture.UiState

data class EventListState(
    val events: List<Event> = emptyList(),
    val isConnected: Boolean = false,
    val expandedIds: Set<String> = emptySet(),
    val selectedType: String? = null,
    val selectedSource: String? = null,
    val availableTypes: Set<String> = emptySet(),
    val availableSources: Set<String> = emptySet(),
    val filteredEvents: List<Event> = emptyList()
) : UiState

sealed interface EventListIntent : UiIntent {
    data class ToggleExpand(val eventId: String) : EventListIntent
    data object Disconnect : EventListIntent
    data class SetTypeFilter(val type: String?) : EventListIntent
    data class SetSourceFilter(val source: String?) : EventListIntent
    data object ClearFilters : EventListIntent
}

sealed interface EventListEffect : UiEffect {
    data object Disconnected : EventListEffect
}


