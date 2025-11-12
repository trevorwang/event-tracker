package mingsin.event

import kotlinx.serialization.Serializable

/**
 * API response data model
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

/**
 * Event creation response
 */
@Serializable
data class EventResponse(
    val success: Boolean,
    val message: String,
    val eventId: String
)

/**
 * Server status response
 */
@Serializable
data class StatusResponse(
    val status: String,
    val appClients: Int,
    val desktopClients: Int
)

