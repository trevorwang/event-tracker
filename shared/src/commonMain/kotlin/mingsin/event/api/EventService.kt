package mingsin.event.api

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingsin.event.Event
import mingsin.event.EventResponse
import mingsin.event.StatusResponse
import mingsin.event.logger.FileLogWriterFactory

/**
 * Event Service
 * Provides high-level API for sending events and managing server connection
 */
class EventService(
    private val apiClientManager: ApiClientManager
) {
    private val logger = FileLogWriterFactory().create()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    /**
     * Get server URL state flow
     */
    val serverUrl: StateFlow<String> = apiClientManager.serverUrl
    
    /**
     * Set server URL
     * @param url Server URL (e.g., "http://localhost:8080" or "https://api.example.com")
     */
    fun setServerUrl(url: String) {
        logger.d { "Setting server URL to: $url" }
        apiClientManager.setServerUrl(url)
    }
    
    /**
     * Get current server URL
     */
    fun getCurrentServerUrl(): String {
        return apiClientManager.getCurrentServerUrl()
    }
    
    /**
     * Send event to server
     * @param event Event to send
     * @return Result with EventResponse on success or error message on failure
     */
    suspend fun sendEvent(event: Event): Result<EventResponse> {
        return try {
            logger.d { "Sending event: id=${event.id}, type=${event.type}, source=${event.source}" }
            val response = apiClientManager.getEventApi().sendEvent(event)
            if (response.success) {
                logger.d { "Event sent successfully: eventId=${response.eventId}" }
                Result.success(response)
            } else {
                logger.w { "Event send failed: ${response.message}" }
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            logger.e(e) { "Failed to send event: ${e.message}" }
            Result.failure(e)
        }
    }
    
    /**
     * Get server status
     * @return Result with StatusResponse on success or error message on failure
     */
    suspend fun getServerStatus(): Result<StatusResponse> {
        return try {
            logger.d { "Getting server status" }
            val response = apiClientManager.getEventApi().getStatus()
            logger.d { "Server status: ${response.status}, appClients=${response.appClients}, desktopClients=${response.desktopClients}" }
            Result.success(response)
        } catch (e: Exception) {
            logger.e(e) { "Failed to get server status: ${e.message}" }
            Result.failure(e)
        }
    }
    
    /**
     * Send event to server asynchronously (non-suspend)
     * Handles coroutine internally
     * @param event Event to send
     * @param onResult Callback with Result<EventResponse> (optional)
     */
    fun sendEventAsync(
        event: Event,
        onResult: ((Result<EventResponse>) -> Unit)? = null
    ) {
        scope.launch {
            val result = sendEvent(event)
            onResult?.invoke(result)
        }
    }
    
    /**
     * Get server status asynchronously (non-suspend)
     * Handles coroutine internally
     * @param onResult Callback with Result<StatusResponse> (optional)
     */
    fun getServerStatusAsync(
        onResult: ((Result<StatusResponse>) -> Unit)? = null
    ) {
        scope.launch {
            val result = getServerStatus()
            onResult?.invoke(result)
        }
    }
}

