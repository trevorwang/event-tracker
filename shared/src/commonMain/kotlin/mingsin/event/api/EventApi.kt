package mingsin.event.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import mingsin.event.Event
import mingsin.event.EventResponse
import mingsin.event.StatusResponse

/**
 * Event API interface using Ktorfit
 */
interface EventApi {
    /**
     * Send event to server
     * @param event Event to send
     * @return EventResponse with success status and event ID
     */
    @POST("api/events")
    suspend fun sendEvent(@Body event: Event): EventResponse

    /**
     * Get server status
     * @return StatusResponse with server status and client counts
     */
    @GET("api/status")
    suspend fun getStatus(): StatusResponse
}

