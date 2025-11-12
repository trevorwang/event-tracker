package mingsin.event.api

/**
 * Factory for creating EventService instance
 * Provides a singleton instance for easy access
 */
object EventServiceFactory {
    private val apiClientManager = ApiClientManager()
    private val eventService = EventService(apiClientManager)
    
    /**
     * Get EventService instance
     */
    fun getEventService(): EventService {
        return eventService
    }
    
    /**
     * Create a new EventService instance with custom ApiClientManager
     */
    fun createEventService(apiClientManager: ApiClientManager = ApiClientManager()): EventService {
        return EventService(apiClientManager)
    }
}

